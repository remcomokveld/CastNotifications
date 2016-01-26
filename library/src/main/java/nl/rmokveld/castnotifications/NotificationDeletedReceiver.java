package nl.rmokveld.castnotifications;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationDeletedReceiver extends BroadcastReceiver {

    public static Intent getDeleteIntent(Context context, int notificationId) {
        return getIntent(context, notificationId, null, false).setAction("delete");
    }

    public static Intent getIntent(Context context, int notificationId, PendingIntent pendingIntent, boolean autoCancel) {
        Intent intent = new Intent(context, NotificationDeletedReceiver.class)
                .putExtra("notificationId", notificationId);
        if (pendingIntent != null) {
            intent.putExtra("pendingIntent", pendingIntent);
            intent.putExtra("autoCancel", autoCancel);
        }
        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra("notificationId", 0);
        if (intent.getBooleanExtra("autoCancel", true))
            CastNotificationManager.getInstance().cancel(notificationId);
        if (intent.hasExtra("pendingIntent")) {
            PendingIntent pendingIntent = intent.getParcelableExtra("pendingIntent");
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException ignored) {
            }
        }
    }
}
