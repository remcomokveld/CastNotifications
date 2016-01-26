package nl.rmokveld.castnotifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class WifiStateReceiver extends BroadcastReceiver {

    private static final String TAG = "WifiStateReceiver";
    private Boolean mWasConnected;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Log.d(TAG, "onWifiStateChanged()");
            boolean isConnected = DeviceStateHelper.isWifiConnected(context);
            if (mWasConnected == null || mWasConnected != isConnected) {
                if (isConnected) {
                    CastNotificationManager.getInstance().getDiscoveryStrategy().onWifiConnected();
                } else {
                    CastNotificationManager.getInstance().getDiscoveryStrategy().onWifiDisconnected();
                }
                mWasConnected = isConnected;
            }
        }
    }
}
