package nl.rmokveld.castnotifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.json.JSONObject;

public interface NotificationBuildCallback {

    /**
     * @param context
     * @param builder The builder of the notification. CastNotifications will add actions for available devices and set a delete intent.
     * @param when
     * @param customData
     */
    void onBuild(Context context, NotificationCompat.Builder builder, int id, String title, String subtitle, long when, @Nullable JSONObject customData, boolean castDevicesAvailable);
    void onBuildForConnecting(Context context, NotificationCompat.Builder builder, int id, String title, long when, @Nullable JSONObject customData, String deviceName);
    void onBuildForError(Context context, NotificationCompat.Builder builder, int id, String title, String subtitle, long when, @Nullable JSONObject customData);

}
