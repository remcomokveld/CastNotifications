package nl.rmokveld.castnotifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class WifiStateReceiver extends BroadcastReceiver {

    private static final String TAG = "WifiStateReceiver";
    private static Boolean sWasConnected;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Log.d(TAG, "onWifiStateChanged()");
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            boolean isConnected = networkInfo.isConnected();
            if (sWasConnected == null || sWasConnected != isConnected) {
                sWasConnected = isConnected;
                if (isConnected) {
                    CastNotificationManager.getInstance().getDiscoveryStrategy().onWifiConnected();
                } else {
                    CastNotificationManager.getInstance().getDiscoveryStrategy().onWifiDisconnected();
                }
            }
        }
    }
}
