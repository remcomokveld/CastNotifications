package nl.rmokveld.castnotifications;

import android.support.v7.media.MediaRouter;

import java.util.Map;

interface DiscoveryStrategy {
    void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route);
    void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route, boolean duringActiveDiscovery);
    void onWifiConnected();
    void onWifiDisconnected();
    void onScreenTurnedOn();
    void onScreenTurnedOff();
    void onActiveNotificationsChanged(boolean hasActiveNotifications);
    Map<String,String> getAvailableRoutes();
    void onBackgroundDiscoveryStopped();
}
