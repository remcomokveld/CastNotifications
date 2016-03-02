package nl.rmokveld.castnotifications.interfaces;

import android.support.annotation.Nullable;
import android.support.v7.media.MediaRouter;

import java.util.Map;

public interface DiscoveryStrategy {
    void onRouteAdded(MediaRouter.RouteInfo route);
    void onWifiConnected();
    void onWifiDisconnected();
    void onScreenTurnedOn();
    void onScreenTurnedOff();
    void setHasActiveNotifications(boolean hasActiveNotifications);
    @Nullable Map<String,String> getAvailableRoutes();
    void onBackgroundDiscoveryTimeout();
}
