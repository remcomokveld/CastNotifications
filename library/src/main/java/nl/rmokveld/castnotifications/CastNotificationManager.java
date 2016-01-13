package nl.rmokveld.castnotifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CastNotificationManager {

    private static final String TAG = "CastNotificationManager";

    private static CastNotificationManager sInstance;

    private final MediaRouteSelector sMediaRouteSelector;

    private final CastCompanionInterface mCastCompanionInterface;
    private final MediaInfoSerializer mMediaInfoSerializer;
    private final Context mContext;
    private final Map<MediaRouter.RouteInfo, CastDevice> mAvailableRoutes = new HashMap<>();
    private final SparseArray<CastNotification> mCastNotifications = new SparseArray<>();
    private final Set<OnRouteAddedListener> mOnRouteAddedListeners = new HashSet<>();
    private NotificationBuilder mNotificationBuilder = new DefaultNotificationBuilder();
    private OnApplicationConnectedListener mOnApplicationConnectedListener;

    private final MediaRouter.Callback mMediaRouterCallback = new MediaRouter.Callback() {

        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
            Log.d(TAG, "onRouteAdded() called with: " + "router = [" + router + "], route = [" + route + "]");
            refreshRoutes(router);
            for (OnRouteAddedListener listener : mOnRouteAddedListeners) {
                if (mAvailableRoutes.containsKey(route))
                    listener.onRouteAdded(route, mAvailableRoutes.get(route));
            }
            postNotifications();
        }

        @Override
        public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
            Log.d(TAG, "onRouteRemoved() called with: " + "router = [" + router + "], route = [" + route + "]");
            refreshRoutes(router);
            postNotifications();
        }
    };
    private final NotificationDatabase mDatabase;

    private CastNotificationManager(Context context,
                                    @NonNull MediaRouteSelector selector,
                                    @NonNull CastCompanionInterface castCompanionInterface,
                                    @NonNull MediaInfoSerializer mediaInfoSerializer) {
        mContext = context.getApplicationContext();
        sMediaRouteSelector = selector;
        mCastCompanionInterface = castCompanionInterface;
        mMediaInfoSerializer = mediaInfoSerializer;
        mDatabase = new NotificationDatabase(mContext);
        mDatabase.getCastNotifications(new NotificationDatabase.Callback() {
            @Override
            public void onComplete(List<CastNotification> castNotifications) {
                for (CastNotification castNotification : castNotifications) {
                    mCastNotifications.put(castNotification.getId(), castNotification);
                }
                postNotifications();
                onActiveNotificationsChanged();
            }
        });
    }

    public static CastNotificationManager getInstance() {
        if (sInstance == null) {
            throw new NullPointerException("CastNotificationManager not initialized");
        }
        return sInstance;
    }

    public static void init(Context context, @NonNull MediaRouteSelector selector, @NonNull CastCompanionInterface castCompanionInterface,
                            @NonNull MediaInfoSerializer mediaInfoSerializer) {
        sInstance = new CastNotificationManager(context, selector, castCompanionInterface, mediaInfoSerializer);
    }

    public void notify(int id, String title, String contentText, @NonNull MediaInfo mediaInfo) {
        Log.d(TAG, "notify() called with: " + "id = [" + id + "], title = [" + title + "], contentText = [" + contentText + "], mediaInfo = [" + mediaInfo + "]");
        CastNotification notification = new CastNotification(id, title, contentText, mediaInfo);
        mCastNotifications.put(id, notification);
        persistNotifications();
        postNotification(notification, mAvailableRoutes.values());
        onActiveNotificationsChanged();
    }

    public void cancel(int notificationId) {
        Log.d(TAG, "cancel() called with: " + "notificationId = [" + notificationId + "]");
        NotificationManagerCompat.from(mContext).cancel("cast_notifications", notificationId);
        mCastNotifications.delete(notificationId);
        mDatabase.delete(notificationId);
        onActiveNotificationsChanged();
    }

    @NonNull
    MediaRouteSelector getMediaRouteSelector() {
        return sMediaRouteSelector;
    }

    @NonNull
    CastCompanionInterface getCastCompanionInterface() {
        return mCastCompanionInterface;
    }

    @NonNull
    public MediaInfoSerializer getMediaInfoSerializer() {
        return mMediaInfoSerializer;
    }

    @SuppressWarnings("unused")
    public void setCustomNotificationBuilder(NotificationBuilder notificationBuilder) {
        mNotificationBuilder = notificationBuilder;
    }

    private NotificationBuilder getNotificationBuilder() {
        return mNotificationBuilder;
    }

    MediaRouter.Callback getMediaRouterCallback() {
        return mMediaRouterCallback;
    }

    Context getContext() {
        return mContext;
    }

    void refreshRoutes(MediaRouter router) {
        Log.d(TAG, "refreshRoutes() called with: " + "router = [" + router + "]");
        mAvailableRoutes.clear();
        for (MediaRouter.RouteInfo routeInfo : router.getRoutes()) {
            if (routeInfo.isDefault()) continue;
            CastDevice castDevice = CastDevice.getFromBundle(routeInfo.getExtras());
            if (castDevice != null && castDevice.isOnLocalNetwork()) {
                Log.d(TAG, "refreshRoutes: route found:"+castDevice.getFriendlyName());
                mAvailableRoutes.put(routeInfo, castDevice);
            }
        }
    }

    private boolean hasActiveNotifications() {
        return mCastNotifications.size() > 0;
    }

    ///// STATE CHANGES
    private void onActiveNotificationsChanged() {
        if (hasActiveNotifications()) {
            if (DeviceStateHelper.isWifiConnected()) {
                DiscoveryService.start(mContext, "", false);
            }
        } else {
            DiscoveryService.stop(mContext);
        }

    }

    void onScreenTurnedOn() {
        if (hasActiveNotifications() && DeviceStateHelper.isWifiConnected()) {
            DiscoveryService.start(mContext, "", false);
        }
    }

    void onScreenTurnedOff() {
        if (hasActiveNotifications() && DeviceStateHelper.isWifiConnected()) {
            DiscoveryService.removeTimeout(mContext);
        }
    }

    void onWifiStateChanged() {
        if (DeviceStateHelper.isWifiConnected()) {
            if (hasActiveNotifications()) {
                DiscoveryService.start(mContext, "", false);
            }
        } else {
            DiscoveryService.stop(mContext);
            mAvailableRoutes.clear();
            postNotifications();
        }
    }

    public void onApplicationConnected() {
        if (mOnApplicationConnectedListener != null)
            mOnApplicationConnectedListener.onApplicationConnected();
    }

    private void postNotifications() {
        for (int i = 0; i < mCastNotifications.size(); i++) {
            postNotification(mCastNotifications.valueAt(i), mAvailableRoutes.values());
        }
    }

    private void postNotification(CastNotification castNotification, Collection<CastDevice> castDevices) {
        Log.d(TAG, "postNotification() called with: " + "castNotification = [" + castNotification + "], castDevices = [" + castDevices + "]");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        CastNotificationManager.getInstance().getNotificationBuilder().build(castNotification, builder);
        builder.setDeleteIntent(PendingIntent.getBroadcast(mContext, 0, new Intent(mContext, NotificationDeletedReceiver.class).setData(Uri.parse("content://" + BuildConfig.APPLICATION_ID + "/notifications/" + castNotification.getId())), PendingIntent.FLAG_UPDATE_CURRENT));
        if (castNotification.getState() == CastNotification.STATE_NORMAL) {
            for (CastDevice castDevice : castDevices) {
                builder.addAction(R.drawable.ic_cast_light, castDevice.getFriendlyName(),
                        PendingIntent.getService(mContext, castNotification.getId(), StartCastService.getIntent(castNotification.getId(), mContext, castDevice, castNotification.getMediaInfo()),
                                PendingIntent.FLAG_UPDATE_CURRENT));
            }
        }
        NotificationManagerCompat.from(mContext).notify("cast_notifications", castNotification.getId(), builder.build());
    }

    void setDiscoveryAlarm() {
        if (hasActiveNotifications() && DeviceStateHelper.isWifiConnected()) {
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            PendingIntent operation = PendingIntent.getService(mContext, 0, DiscoveryService.buildIntent(mContext, "", false), PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                alarmManager.setWindow(AlarmManager.RTC, 1000 * 60 * 60, 1000 * 60 * 10, operation);
            else
                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000 * 60 * 60, operation);
        }
    }

    void setOnApplicationConnectedListener(OnApplicationConnectedListener onApplicationConnectedListener) {
        mOnApplicationConnectedListener = onApplicationConnectedListener;
    }

    void addOnRouteAddedListener(OnRouteAddedListener listener) {
        mOnRouteAddedListeners.add(listener);
    }

    void removeOnRouteAddedListener(OnRouteAddedListener listener) {
        mOnRouteAddedListeners.remove(listener);
    }

    void setStateConnecting(int notificationId, String deviceName) {
        Log.d(TAG, "setStateConnecting() called with: " + "notificationId = [" + notificationId + "], deviceName = [" + deviceName + "]");
        CastNotification castNotification = mCastNotifications.get(notificationId);
        castNotification.setState(CastNotification.STATE_CONNECTING, deviceName);
        persistNotifications();
        postNotifications();
    }

    private void persistNotifications() {
        mDatabase.persistNotifications(mCastNotifications);
    }

    interface OnApplicationConnectedListener {
        void onApplicationConnected();
    }

    interface OnRouteAddedListener {
        @UiThread
        void onRouteAdded(MediaRouter.RouteInfo routeInfo, CastDevice castDevice);
    }
}
