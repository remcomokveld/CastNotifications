package nl.rmokveld.castnotifications;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;

class DeviceStateHelper {

    static boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) CastNotificationManager.getInstance().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
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

    static boolean isScreenTurnedOn() {
        PowerManager powerManager = (PowerManager) CastNotificationManager.getInstance().getContext().getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH)
            return powerManager.isInteractive();
        //noinspection deprecation
        return powerManager.isScreenOn();
    }
}
