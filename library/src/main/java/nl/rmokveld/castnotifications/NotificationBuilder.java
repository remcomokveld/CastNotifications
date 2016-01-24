package nl.rmokveld.castnotifications;

import android.content.Context;
import android.support.v4.app.NotificationCompat;

public interface NotificationBuilder {

    /**
     * @param context
     * @param builder The builder of the notification. CastNotifications will add actions for available devices and set a delete intent.
     */
    void build(Context context, NotificationCompat.Builder builder, int id, String title, String subtitle, boolean castDevicesAvailable);
    void buildForConnecting(Context context, NotificationCompat.Builder builder, int id, String title, String deviceName);
    void buildForError(Context context, NotificationCompat.Builder builder, int id, String title, String subtitle);
}
