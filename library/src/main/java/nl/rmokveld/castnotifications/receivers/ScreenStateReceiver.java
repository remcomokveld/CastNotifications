package nl.rmokveld.castnotifications.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import nl.rmokveld.castnotifications.CastNotificationManager;

public class ScreenStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            CastNotificationManager.getInstance().getDiscoveryStrategy().onScreenTurnedOn();
        } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            CastNotificationManager.getInstance().getDiscoveryStrategy().onScreenTurnedOff();
        }
    }
}
