package nl.rmokveld.castnotifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            CastNotificationManager.getInstance().onScreenTurnedOn();
        } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            CastNotificationManager.getInstance().onScreenTurnedOff();
        }
    }
}
