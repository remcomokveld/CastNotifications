package nl.rmokveld.castnotifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.HashMap;
import java.util.Map;

public class WifiStateReceiver extends BroadcastReceiver {

    private static final String TAG = "WifiStateReceiver";
    private static Boolean sWasConnected;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Map<String, Object> extraData = new HashMap<>();
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
            if (intent.hasExtra(WifiManager.EXTRA_NETWORK_INFO)) {
                HashMap<String, Object> networkInfoMap = new HashMap<>(4);
                networkInfoMap.put("isConnected", networkInfo.isConnected());
                networkInfoMap.put("isAvailable", networkInfo.isAvailable());
                networkInfoMap.put("type", networkInfo.getTypeName());
                networkInfoMap.put("subtype", networkInfo.getSubtypeName());
                extraData.put("networkInfo", networkInfoMap);
            }
            if (intent.hasExtra(WifiManager.EXTRA_BSSID)) {
                extraData.put("BSSID", intent.getStringExtra(WifiManager.EXTRA_BSSID));
            }
            if ((intent.hasExtra(WifiManager.EXTRA_WIFI_INFO))) {
                HashMap<String, Object> wifiInfoMap = new HashMap<>();
                wifiInfoMap.put("supplicantState", wifiInfo.getSupplicantState().toString());
                wifiInfoMap.put("value", wifiInfo.toString());
                extraData.put("wifiInfo", wifiInfoMap);

            }
            Log.d(TAG, "onWifiStateChanged()", extraData);
            boolean isConnected = networkInfo.isConnected();
            Log.d(TAG, "sWasConnected: "+ sWasConnected + ", isConnected: "+isConnected);
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
