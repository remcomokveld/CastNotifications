package nl.rmokveld.castnotifications;

import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.json.JSONObject;

public class DefaultNotificationBuildCallback implements NotificationBuildCallback {

    private boolean mSearchActionOnWear = true;

    @Override
    public void onBuild(Context context, NotificationCompat.Builder builder, int id, String title, String subtitle, long when, @Nullable JSONObject customData, boolean castDevicesAvailable) {
        builder.setContentTitle(title)
                .setSmallIcon(R.drawable.ic_cast_dark)
                .setAutoCancel(true)
                .setWhen(when)
                .setOnlyAlertOnce(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        if (mSearchActionOnWear && !castDevicesAvailable) {
            // If there are no notifications add an action on wear to start active discovery. Because of the wakelocks the device will wake up which is needed for discovery
            builder.extend(new NotificationCompat.WearableExtender()
                    .addAction(new NotificationCompat.Action(R.drawable.ic_cast_light, context.getString(R.string.cast_notifications_discovering),
                            PendingIntent.getService(context, 0, DiscoveryService.buildIntent(context, true, "wear"), PendingIntent.FLAG_UPDATE_CURRENT))));
        }
        if (subtitle != null)
            builder.setContentText(subtitle);
    }

    @Override
    public void onBuildForConnecting(Context context, NotificationCompat.Builder builder, int id, String title, long when, @Nullable JSONObject customData, String deviceName) {
        onBuild(context, builder, id, title, null, when, customData, true);
        builder.setContentText(context.getString(R.string.cast_notifications_connecting, deviceName));
    }

    @Override
    public void onBuildForError(Context context, NotificationCompat.Builder builder, int id, String title, String subtitle, long when, @Nullable JSONObject customData) {
        mSearchActionOnWear = false;
        onBuild(context, builder, id, title, subtitle, when, customData, false);
        mSearchActionOnWear = true;
    }


}
