package nl.rmokveld.castnotifications;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;

class DeviceStateHelper {

    static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean wifiConnected = false;
            for (Network network : connectivityManager.getAllNetworks()) {
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                wifiConnected |= networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected();
            }
            return wifiConnected;
        } else {
            //noinspection deprecation
            return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
        }
    }

    static boolean isScreenTurnedOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH)
            return powerManager.isInteractive();
        //noinspection deprecation
        return powerManager.isScreenOn();
    }
}
