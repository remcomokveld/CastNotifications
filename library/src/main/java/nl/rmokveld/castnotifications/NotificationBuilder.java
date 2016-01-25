package nl.rmokveld.castnotifications;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.json.JSONObject;

public interface NotificationBuilder {

    /**
     * @param context
     * @param builder The builder of the notification. CastNotifications will add actions for available devices and set a delete intent.
     * @param when
     * @param customData
     */
    void build(Context context, NotificationCompat.Builder builder, int id, String title, String subtitle, long when, @Nullable JSONObject customData, boolean castDevicesAvailable);
    void buildForConnecting(Context context, NotificationCompat.Builder builder, int id, String title, long when, @Nullable JSONObject customData, String deviceName);
    void buildForError(Context context, NotificationCompat.Builder builder, int id, String title, String subtitle, long when, @Nullable JSONObject customData);
}
