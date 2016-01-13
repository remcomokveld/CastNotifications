package nl.rmokveld.castnotifications;

import android.support.v7.app.NotificationCompat;

public interface NotificationBuilder {

    /**
     * @param castNotification The CastNotification for which the notification has to be build.
     * @param builder The builder of the notification. CastNotifications will add actions for available devices and set a delete intent.
     */
    void build(CastNotification castNotification, NotificationCompat.Builder builder);

}
