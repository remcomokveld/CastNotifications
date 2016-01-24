package nl.rmokveld.castnotifications;

import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

class DefaultNotificationBuilder implements NotificationBuilder {

    @Override
    public void build(Context context, NotificationCompat.Builder builder, int id, String title, String subtitle, boolean castDevicesAvailable) {
        builder.setContentTitle(title)
                .setSmallIcon(R.drawable.ic_cast_dark)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        if (subtitle != null)
            builder.setContentText(subtitle);
    }

    @Override
    public void buildForConnecting(Context context, NotificationCompat.Builder builder, int id, String title, String deviceName) {
        build(context, builder, id, title, null, true);
        builder.setContentText(context.getString(R.string.cast_notifications_connecting, deviceName));
    }

    @Override
    public void buildForError(Context context, NotificationCompat.Builder builder, int id, String title, String subtitle) {
        build(context, builder, id, title, subtitle, false);
    }


}
