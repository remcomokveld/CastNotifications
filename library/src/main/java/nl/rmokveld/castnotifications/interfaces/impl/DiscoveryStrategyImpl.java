package nl.rmokveld.castnotifications.interfaces.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.CastDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.rmokveld.castnotifications.interfaces.DiscoveryStrategy;
import nl.rmokveld.castnotifications.receivers.ScreenStateReceiver;
import nl.rmokveld.castnotifications.receivers.WifiStateReceiver;
import nl.rmokveld.castnotifications.services.DiscoveryService;
import nl.rmokveld.castnotifications.services.NotificationService;
import nl.rmokveld.castnotifications.utils.DeviceStateHelper;
import nl.rmokveld.castnotifications.utils.Log;

public class DiscoveryStrategyImpl implements DiscoveryStrategy {

    private static final String TAG = "DiscoveryStrategyImpl";
    private final Context mContext;

    private Map<String, String> mAvailableRoutes = new HashMap<>();
    private boolean mHasActiveNotifications;

    public DiscoveryStrategyImpl(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void onRouteAdded(MediaRouter.RouteInfo route) {
        Log.d(TAG, "onRouteAdded() called with: " + "route = [" + route + "]");
        if (route.isDefault()) return;
        CastDevice castDevice = CastDevice.getFromBundle(route.getExtras());
        if (castDevice == null || !castDevice.isOnLocalNetwork()) return;
        if (!mAvailableRoutes.containsKey(route.getId())) {
            mAvailableRoutes.put(route.getId(), castDevice.getFriendlyName());
            onRoutesChanged();
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
        DiscoveryService.stop(mContext);
        onRoutesChanged();
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
    public void setHasActiveNotifications(boolean hasActiveNotifications) {
        Log.d(TAG, "setHasActiveNotifications() called with: " + "hasActiveNotifications = [" + hasActiveNotifications + "]");
        mHasActiveNotifications = hasActiveNotifications;
        if (hasActiveNotifications && DeviceStateHelper.isWifiConnected(mContext)) {
            if (DeviceStateHelper.isWifiConnected(mContext)) {
                DiscoveryService.start(mContext, "activeNotificationChanged");
            }
        } else {
            DiscoveryService.stop(mContext);
        }
        int newState = mHasActiveNotifications ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        mContext.getPackageManager().setComponentEnabledSetting(new ComponentName(mContext, WifiStateReceiver.class), newState, PackageManager.DONT_KILL_APP);
        mContext.getPackageManager().setComponentEnabledSetting(new ComponentName(mContext, ScreenStateReceiver.class), newState, PackageManager.DONT_KILL_APP);
    }

    public Map<String, String> getAvailableRoutes() {
        if (!DeviceStateHelper.isWifiConnected(mContext)) return null;
        List<MediaRouter.RouteInfo> currentMediaRoutes = getMediaRouterRoutes();

        Map<String, String> currentRoutesMap = new HashMap<>(currentMediaRoutes.size());

        for (MediaRouter.RouteInfo routeInfo : currentMediaRoutes) {
            CastDevice castDevice = CastDevice.getFromBundle(routeInfo.getExtras());
            if (castDevice == null || !castDevice.isOnLocalNetwork()) continue;
            currentRoutesMap.put(routeInfo.getId(), castDevice.getFriendlyName());
        }
        if (currentRoutesMap.size() > 0) {
            // If there are routes available the cached routes are not valid anymore, except for
            // those which will be added after this if statement
            mAvailableRoutes.clear();
        }
        mAvailableRoutes.putAll(currentRoutesMap);

        return DeviceStateHelper.isWifiConnected(mContext) ? mAvailableRoutes : null;
    }

    /**
     * MediaRouter functions are only allowed to be called from the UI thread
     * If this method is called from the main thread it will return the available routes
     * immediately else post a runnable on the main thread which fetches the routes the calling
     * thread will then wait for the main thread to finish
     *
     * @return
     */
    private List<MediaRouter.RouteInfo> getMediaRouterRoutes() {
        if (Looper.getMainLooper() == Looper.myLooper())
            return MediaRouter.getInstance(mContext).getRoutes();
        final List<MediaRouter.RouteInfo> routes = new ArrayList<>(5);
        final AtomicBoolean called = new AtomicBoolean(false);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                routes.addAll(MediaRouter.getInstance(mContext).getRoutes());
                synchronized (called) {
                    called.set(true);
                }
            }
        });
        while (!called.get()) {
            try {
                synchronized (this) {
                    wait(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return routes;
    }

    protected void onRoutesChanged() {
        NotificationService.update(mContext);
    }

    @Override
    public void onBackgroundDiscoveryTimeout() {
        Set<String> availableRouteIds = new HashSet<>();
        for (MediaRouter.RouteInfo routeInfo : MediaRouter.getInstance(mContext).getRoutes()) {
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
            onRoutesChanged();
    }
}
