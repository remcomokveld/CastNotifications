package nl.rmokveld.castnotifications;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.MediaInfo;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CastNotificationManager {

    private static final String TAG = "CastNotificationManager";

    private static CastNotificationManager sInstance;

    private final Context mContext;

    private final CastCompanionInterface mCastCompanionInterface;
    private NotificationBuildCallback mNotificationBuildCallback = new DefaultNotificationBuildCallback();

    private final DiscoveryStrategy mDiscoveryStrategy;
    private final NotificationHelper mNotificationHelper;
    private final NotificationDatabase mNotificationDatabase;

    private final Set<OnApplicationConnectedListener> mOnApplicationConnectedListener = new HashSet<>();

    private CastNotificationManager(Context context,
                                    @NonNull CastCompanionInterface castCompanionInterface) {
        mContext = context.getApplicationContext();
        mCastCompanionInterface = castCompanionInterface;
        mDiscoveryStrategy = new DiscoveryStrategyImpl(mContext) {
            @Override
            void onRoutesChanged(Map<String, String> availableRoutes) {
                Log.d(TAG, "onRoutesChanged() called with: " + "availableRoutes = [" + availableRoutes + "]");
                mNotificationHelper.postNotifications(availableRoutes);
            }
        };
        mNotificationHelper = new NotificationHelperImpl(mContext);
        mNotificationDatabase = new NotificationDatabase(mContext);
        mNotificationDatabase.getCastNotifications(new NotificationDatabase.Callback() {
            @Override
            public void onComplete(List<CastNotification> castNotifications) {
                mNotificationHelper.addAll(castNotifications, mDiscoveryStrategy.getAvailableRoutes());
                mDiscoveryStrategy.onActiveNotificationsChanged(true);
            }
        });

        MediaRouter mediaRouter = MediaRouter.getInstance(mContext);
        mediaRouter.addCallback(mCastCompanionInterface.getMediaRouteSelector(), new MediaRouter.Callback() {
                @Override
                public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
                    super.onRouteAdded(router, route);
                    mDiscoveryStrategy.onRouteAdded(router, route);
                }

                @Override
                public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
                    super.onRouteRemoved(router, route);
                    mDiscoveryStrategy.onRouteRemoved(router, route, false);
                }
            });
        for (MediaRouter.RouteInfo routeInfo : mediaRouter.getRoutes()) {
            mDiscoveryStrategy.onRouteAdded(mediaRouter, routeInfo);
        }
    }

    public static void init(Context context, @NonNull CastCompanionInterface castCompanionInterface) {
        sInstance = new CastNotificationManager(context, castCompanionInterface);
    }

    public static CastNotificationManager getInstance() {
        if (sInstance == null) {
            throw new NullPointerException("CastNotificationManager not initialized");
        }
        return sInstance;
    }

    public void notify(int id, String title, String contentText, @NonNull MediaInfo mediaInfo, @Nullable JSONObject customData) {
        Log.d(TAG, "notify() called with: " + "id = [" + id + "], title = [" + title + "], contentText = [" + contentText + "], mediaInfo = [" + mediaInfo + "]");
        CastNotification notification = new CastNotification(id, title, contentText, System.currentTimeMillis(), mediaInfo, customData);
        mNotificationHelper.add(notification, mDiscoveryStrategy.getAvailableRoutes());
        mNotificationDatabase.persistNotification(notification);
        mDiscoveryStrategy.onActiveNotificationsChanged(mNotificationHelper.hasActiveNotifications());
    }

    public void cancel(int notificationId) {
        Log.d(TAG, "cancel() called with: " + "notificationId = [" + notificationId + "]");
        mNotificationHelper.cancel(notificationId);
        mNotificationDatabase.delete(notificationId);
        mDiscoveryStrategy.onActiveNotificationsChanged(mNotificationHelper.hasActiveNotifications());
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
    public void setCustomNotificationBuildCallback(NotificationBuildCallback callback) {
        mNotificationBuildCallback = callback;
    }

    NotificationBuildCallback getNotificationBuildCallback() {
        return mNotificationBuildCallback;
    }

    DiscoveryStrategy getDiscoveryStrategy() {
        return mDiscoveryStrategy;
    }

    Context getContext() {
        return mContext;
    }

    public void onApplicationConnected() {
        for (OnApplicationConnectedListener listener : mOnApplicationConnectedListener) {
            listener.onApplicationConnected();
        }
    }

    void addOnApplicationConnectedListener(OnApplicationConnectedListener listener) {
        mOnApplicationConnectedListener.add(listener);
    }

    void removeOnApplicationConnectedListener(OnApplicationConnectedListener listener) {
        mOnApplicationConnectedListener.remove(listener);
    }

    interface OnApplicationConnectedListener {
        void onApplicationConnected();
    }

}
