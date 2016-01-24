package nl.rmokveld.castnotifications;

import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.CastDevice;

import java.util.HashMap;
import java.util.Map;

/**
 * CastAvailabilityHelper delays removal of routes and keeps track of current rounds
 */
public abstract class CastAvailabilityHelper {

    private Map<String, String> mAvailableRoutes = new HashMap<>();
    private final Handler mHandler = new Handler();
    private final Runnable mOnRouteRemovedRunnable = new Runnable() {
        @Override
        public void run() {

        }
    };

    public CastAvailabilityHelper() {
    }

    public void onRouteAdded(MediaRouter.RouteInfo route) {
        if (route.isDefault()) return;
        CastDevice castDevice = CastDevice.getFromBundle(route.getExtras());
        if (castDevice == null || !castDevice.isOnLocalNetwork()) return;
        if (!mAvailableRoutes.containsKey(route.getId())) {
            mHandler.removeCallbacksAndMessages(route.getId());
            mAvailableRoutes.put(route.getId(), castDevice.getFriendlyName());
            onRoutesChanged(mAvailableRoutes);
        }
    }

    public void onRouteRemoved(final MediaRouter router, final MediaRouter.RouteInfo route) {
        if (!mAvailableRoutes.containsKey(route.getId())) return;
        mHandler.postAtTime(new OnRouteRemovedRunnable(router, route.getId()), route.getId(), SystemClock.uptimeMillis() + 3000);
    }

    public void onWifiDisconnected() {
        mHandler.removeCallbacksAndMessages(null);
        mAvailableRoutes.clear();
        onRoutesChanged(mAvailableRoutes);
    }

    public Map<String, String> getAvailableRoutes() {
        return mAvailableRoutes;
    }

    abstract void onRoutesChanged(Map<String, String> availableRoutes);

    public void refreshRoutes(MediaRouter mediaRouter) {
        for (String routeId : mAvailableRoutes.keySet()) {
            mHandler.postAtTime(new OnRouteRemovedRunnable(mediaRouter, routeId), routeId, SystemClock.uptimeMillis() + 3000);
        }
        for (MediaRouter.RouteInfo routeInfo : mediaRouter.getRoutes()) {
            onRouteAdded(routeInfo);
        }
    }

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
}
