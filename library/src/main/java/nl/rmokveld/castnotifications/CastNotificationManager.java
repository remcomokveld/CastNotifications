package nl.rmokveld.castnotifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.SparseArray;

import com.google.android.gms.cast.MediaInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CastNotificationManager {

    private static final String TAG = "CastNotificationManager";

    private static CastNotificationManager sInstance;

    private final CastCompanionInterface mCastCompanionInterface;
    private final Context mContext;
    private final SparseArray<CastNotification> mCastNotifications = new SparseArray<>();
    private final CastAvailabilityHelper mCastAvailabilityHelper;
    private final Set<OnApplicationConnectedListener> mOnApplicationConnectedListener = new HashSet<>();
    private NotificationBuilder mNotificationBuilder = new DefaultNotificationBuilder();

    private final MediaRouter.Callback mMediaRouterCallback = new MediaRouter.Callback() {

        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
            Log.d(TAG, "onRouteAdded() called with: " + "router = [" + router + "], route = [" + route + "]");
            mCastAvailabilityHelper.onRouteAdded(route);
        }

        @Override
        public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
            Log.d(TAG, "onRouteRemoved() called with: " + "router = [" + router + "], route = [" + route + "]");
            mCastAvailabilityHelper.onRouteRemoved(router, route);
        }
    };

    private final NotificationDatabase mDatabase;

    private CastNotificationManager(Context context,
                                    @NonNull CastCompanionInterface castCompanionInterface) {
        mContext = context.getApplicationContext();
        mCastCompanionInterface = castCompanionInterface;
        mDatabase = new NotificationDatabase(mContext);
        mDatabase.getCastNotifications(new NotificationDatabase.Callback() {
            @Override
            public void onComplete(List<CastNotification> castNotifications) {
                for (CastNotification castNotification : castNotifications) {
                    mCastNotifications.put(castNotification.getId(), castNotification);
                }
                postNotifications(mCastAvailabilityHelper.getAvailableRoutes());
                onActiveNotificationsChanged();
            }
        });
        mCastAvailabilityHelper = new CastAvailabilityHelper() {
            @Override
            void onRoutesChanged(Map<String, String> availableRoutes) {
                postNotifications(availableRoutes);
            }
        };
    }

    public static CastNotificationManager getInstance() {
        if (sInstance == null) {
            throw new NullPointerException("CastNotificationManager not initialized");
        }
        return sInstance;
    }

    public static void init(Context context, @NonNull CastCompanionInterface castCompanionInterface) {
        sInstance = new CastNotificationManager(context, castCompanionInterface);
    }

    public void notify(int id, String title, String contentText, @NonNull MediaInfo mediaInfo) {
        Log.d(TAG, "notify() called with: " + "id = [" + id + "], title = [" + title + "], contentText = [" + contentText + "], mediaInfo = [" + mediaInfo + "]");
        CastNotification notification = new CastNotification(id, title, contentText, mediaInfo);
        mCastNotifications.put(id, notification);
        persistNotifications();
        postNotification(notification, mCastAvailabilityHelper.getAvailableRoutes());
        onActiveNotificationsChanged();
    }

    public void cancel(int notificationId) {
        Log.d(TAG, "cancel() called with: " + "notificationId = [" + notificationId + "]");
        NotificationManagerCompat.from(mContext).cancel("cast_notifications", notificationId);
        mCastNotifications.delete(notificationId);
        mDatabase.delete(notificationId);
        onActiveNotificationsChanged();
    }

    public void setLogLevel(int level) {
        Log.setLevel(level);
    }

    @NonNull
    MediaRouteSelector getMediaRouteSelector() {
        return mCastCompanionInterface.getMediaRouteSelector();
    }

    @NonNull
    CastCompanionInterface getCastCompanionInterface() {
        return mCastCompanionInterface;
    }

    @SuppressWarnings("unused")
    public void setCustomMediaInfoSerializer(MediaInfoSerializer mediaInfoSerializer) {
        CastNotification.setMediaInfoSerializer(mediaInfoSerializer);
    }

    @SuppressWarnings("unused")
    public void setCustomNotificationBuilder(NotificationBuilder notificationBuilder) {
        mNotificationBuilder = notificationBuilder;
    }

    NotificationBuilder getNotificationBuilder() {
        return mNotificationBuilder;
    }

    MediaRouter.Callback getMediaRouterCallback() {
        return mMediaRouterCallback;
    }

    Context getContext() {
        return mContext;
    }

    void refreshRoutes(MediaRouter mediaRouter) {
        mCastAvailabilityHelper.refreshRoutes(mediaRouter);
    }

    private boolean hasActiveNotifications() {
        return mCastNotifications.size() > 0;
    }

    ///// STATE CHANGES
    private void onActiveNotificationsChanged() {
        Log.d(TAG, "onActiveNotificationsChanged() called with: " + "");
        if (hasActiveNotifications()) {
            if (DeviceStateHelper.isWifiConnected(mContext)) {
                DiscoveryService.start(mContext, "active notifications changed");
            }
        } else {
            DiscoveryService.stop(mContext);
        }

    }

    void onScreenTurnedOn() {
        Log.d(TAG, "onScreenTurnedOn()");
        if (hasActiveNotifications() && DeviceStateHelper.isWifiConnected(mContext)) {
            DiscoveryService.setTimeout(mContext);
        }
    }

    void onScreenTurnedOff() {
        Log.d(TAG, "onScreenTurnedOff()");
        if (hasActiveNotifications() && DeviceStateHelper.isWifiConnected(mContext)) {
            DiscoveryService.removeTimeout(mContext);
        }
    }

    void onWifiStateChanged() {
        Log.d(TAG, "onWifiStateChanged()");
        if (DeviceStateHelper.isWifiConnected(mContext)) {
            if (hasActiveNotifications()) {
                DiscoveryService.start(mContext, "wifi state changed");
            }
        } else {
            mCastAvailabilityHelper.onWifiDisconnected();
        }
    }

    public void onApplicationConnected() {
        for (OnApplicationConnectedListener listener : mOnApplicationConnectedListener) {
            listener.onApplicationConnected();
        }
    }

    private void postNotifications(Map<String, String> availableRoutes) {
        for (int i = 0; i < mCastNotifications.size(); i++) {
            postNotification(mCastNotifications.valueAt(i), availableRoutes);
        }
    }

    private void postNotification(CastNotification castNotification, Map<String, String> castDevices) {
        Log.d(TAG, "postNotification() called with: " + "castNotification = [" + castNotification + "], castDevices = [" + castDevices + "]");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        CastNotificationManager.getInstance().getNotificationBuilder().build(mContext, builder, castNotification.getId(), castNotification.getTitle(), castNotification.getContentText(), castDevices.size() > 0);
        builder.setDeleteIntent(PendingIntent.getBroadcast(mContext, 0, new Intent(mContext, NotificationDeletedReceiver.class).setData(Uri.parse("content://" + BuildConfig.APPLICATION_ID + "/notifications/" + castNotification.getId())), PendingIntent.FLAG_UPDATE_CURRENT));
        for (String routeId : castDevices.keySet()) {
            builder.addAction(R.drawable.ic_cast_light, castDevices.get(routeId),
                    PendingIntent.getService(mContext, castNotification.getId(), StartCastService.getIntent(castNotification, mContext, routeId, castDevices.get(routeId)),
                            PendingIntent.FLAG_UPDATE_CURRENT));
        }
        NotificationManagerCompat.from(mContext).notify("cast_notifications", castNotification.getId(), builder.build());
    }

    void setDiscoveryAlarm() {
        if (hasActiveNotifications() && DeviceStateHelper.isWifiConnected(mContext)) {
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            PendingIntent operation = PendingIntent.getService(mContext, 0, DiscoveryService.buildIntent(mContext, "alarm_service"), PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                alarmManager.setWindow(AlarmManager.RTC, 1000 * 60 * 60, 1000 * 60 * 10, operation);
            else
                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000 * 60 * 60, operation);
        }
    }

    void addOnApplicationConnectedListener(OnApplicationConnectedListener listener) {
        mOnApplicationConnectedListener.add(listener);
    }

    void removeOnApplicationConnectedListener(OnApplicationConnectedListener listener) {
        mOnApplicationConnectedListener.remove(listener);
    }

    private void persistNotifications() {
        mDatabase.persistNotifications(mCastNotifications);
    }

    interface OnApplicationConnectedListener {
        void onApplicationConnected();
    }

}
