package nl.rmokveld.castnotifications;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.CastDevice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

abstract class DiscoveryStrategyImpl implements DiscoveryStrategy {

    private static final String TAG = "DiscoveryStrategyImpl";
    private final Context mContext;
    private final MediaRouter mMediaRouter;

    private Map<String, String> mAvailableRoutes = new HashMap<>();
    private final Handler mHandler = new Handler();
    private boolean mHasActiveNotifications;

    public DiscoveryStrategyImpl(Context context) {
        mContext = context.getApplicationContext();
        mMediaRouter = MediaRouter.getInstance(mContext);
    }

    @Override
    public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
        Log.d(TAG, "onRouteAdded() called with: " + "router = [" + router + "], route = [" + route + "]");
        if (route.isDefault()) return;
        CastDevice castDevice = CastDevice.getFromBundle(route.getExtras());
        if (castDevice == null || !castDevice.isOnLocalNetwork()) return;
        if (!mAvailableRoutes.containsKey(route.getId())) {
            mHandler.removeCallbacksAndMessages(route.getId());
            mAvailableRoutes.put(route.getId(), castDevice.getFriendlyName());
            onRoutesChanged(mAvailableRoutes);
        }
    }

    @Override
    public void onRouteRemoved(final MediaRouter router, final MediaRouter.RouteInfo route, boolean duringActiveDiscovery) {
        Log.d(TAG, "onRouteRemoved() called with: " + "router = [" + router + "], route = [" + route + "], duringActiveDiscovery = [" + duringActiveDiscovery + "]");
        if (duringActiveDiscovery) {
            if (!mAvailableRoutes.containsKey(route.getId())) return;
            mHandler.postAtTime(new OnRouteRemovedRunnable(router, route.getId()), route.getId(), SystemClock.uptimeMillis() + 3000);
        }
    }

    @Override
    public void onWifiConnected() {
        if (mHasActiveNotifications) {
            DiscoveryService.start(mContext, "wifi state changed");
        }
    }

    @Override
    public void onWifiDisconnected() {
        mHandler.removeCallbacksAndMessages(null);
        mAvailableRoutes.clear();
        onRoutesChanged(mAvailableRoutes);
    }

    @Override
    public void onScreenTurnedOn() {
        Log.d(TAG, "onScreenTurnedOn()");
        if (mHasActiveNotifications && DeviceStateHelper.isWifiConnected(mContext)) {
            DiscoveryService.setTimeout(mContext);
        }
    }

    @Override
    public void onScreenTurnedOff() {
        Log.d(TAG, "onScreenTurnedOff()");
        if (mHasActiveNotifications && DeviceStateHelper.isWifiConnected(mContext)) {
            DiscoveryService.removeTimeout(mContext);
        }
    }

    @Override
    public void onActiveNotificationsChanged(boolean hasActiveNotifications) {
        mHasActiveNotifications = hasActiveNotifications;
        Log.d(TAG, "onActiveNotificationsChanged() called with: " + "");
        if (mHasActiveNotifications) {
            if (DeviceStateHelper.isWifiConnected(mContext)) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (mMediaRouter.isRouteAvailable(CastNotificationManager.getInstance().getMediaRouteSelector(), 0))
                            Log.d(TAG, "Routes are already available, no discovery needed needed");
                        else
                            DiscoveryService.start(mContext, "active notifications changed");
                    }
                });
            }
        } else {
            DiscoveryService.stop(mContext);
        }
    }

    public Map<String, String> getAvailableRoutes() {
        return mAvailableRoutes;
    }

    abstract void onRoutesChanged(Map<String, String> availableRoutes);

    public class OnRouteRemovedRunnable implements Runnable {

        private MediaRouter mMediaRouter;
        private String mRouteId;

        public OnRouteRemovedRunnable(MediaRouter mediaRouter, String routeId) {
            mMediaRouter = mediaRouter;
            mRouteId = routeId;
        }

        @Override
        public void run() {
            for (MediaRouter.RouteInfo routeInfo : mMediaRouter.getRoutes()) {
                if (mRouteId.equals(routeInfo.getId())) {
                    return;
                }
            }
            mAvailableRoutes.remove(mRouteId);
            onRoutesChanged(mAvailableRoutes);
        }
    }

    @Override
    public void onBackgroundDiscoveryStopped() {
        Set<String> availableRouteIds = new HashSet<>();
        for (MediaRouter.RouteInfo routeInfo : mMediaRouter.getRoutes()) {
             availableRouteIds.add(routeInfo.getId());
        }
        boolean changed = false;
        for (String routeId : mAvailableRoutes.keySet()) {
            if (!availableRouteIds.contains(routeId)) {
                mAvailableRoutes.remove(routeId);
                changed = true;
            }
        }
        if (changed)
            onRoutesChanged(mAvailableRoutes);
    }
}
