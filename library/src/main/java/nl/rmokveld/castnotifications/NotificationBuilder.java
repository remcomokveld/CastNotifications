package nl.rmokveld.castnotifications;

import android.content.Context;
import android.support.v4.app.NotificationCompat;

public interface NotificationBuilder {

    /**
     * @param context
     * @param castNotification The CastNotification for which the notification has to be build.
     * @param builder The builder of the notification. CastNotifications will add actions for available devices and set a delete intent.
     */
    void build(Context context, CastNotification castNotification, NotificationCompat.Builder builder);

}
