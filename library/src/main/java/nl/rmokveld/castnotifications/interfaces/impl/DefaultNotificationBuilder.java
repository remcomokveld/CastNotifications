package nl.rmokveld.castnotifications.interfaces.impl;

import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.json.JSONObject;

import nl.rmokveld.castnotifications.R;
import nl.rmokveld.castnotifications.interfaces.NotificationBuilder;
import nl.rmokveld.castnotifications.services.DiscoveryService;

public class DefaultNotificationBuilder implements NotificationBuilder {

    private boolean mSearchActionOnWear = true;

    @Override
    public void build(Context context, NotificationCompat.Builder builder, int id, String title, String subtitle, long when, @Nullable JSONObject customData, boolean castDevicesAvailable) {
        builder.setContentTitle(title)
                .setSmallIcon(R.drawable.ic_cast_dark)
                .setAutoCancel(true)
                .setWhen(when)
                .setOnlyAlertOnce(true);
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
    public void buildForConnecting(Context context, NotificationCompat.Builder builder, int id, String title, long when, @Nullable JSONObject customData, String deviceName) {
        build(context, builder, id, title, null, when, customData, true);
        builder.setContentText(context.getString(R.string.cast_notifications_connecting, deviceName));
    }

    @Override
    public void buildForError(Context context, NotificationCompat.Builder builder, int id, String title, String subtitle, long when, @Nullable JSONObject customData) {
        mSearchActionOnWear = false;
        build(context, builder, id, title, subtitle, when, customData, false);
        mSearchActionOnWear = true;
    }


}
