package nl.rmokveld.castnotifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationDeletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = Integer.parseInt(intent.getData().getLastPathSegment());
        CastNotificationManager.getInstance().cancel(notificationId);
    }
}
