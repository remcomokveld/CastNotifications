package nl.rmokveld.castnotifications.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import nl.rmokveld.castnotifications.CastNotificationManager;
import nl.rmokveld.castnotifications.utils.DeviceStateHelper;
import nl.rmokveld.castnotifications.utils.Log;

public class WifiStateReceiver extends BroadcastReceiver {

    private static final String TAG = "WifiStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Log.d(TAG, "onWifiStateChanged()");
            if (DeviceStateHelper.isWifiConnected(context)) {
                CastNotificationManager.getInstance().getDiscoveryStrategy().onWifiConnected();
            } else {
                CastNotificationManager.getInstance().getDiscoveryStrategy().onWifiDisconnected();
            }
        }
    }
}
