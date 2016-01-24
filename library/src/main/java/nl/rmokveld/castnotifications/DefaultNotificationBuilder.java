package nl.rmokveld.castnotifications;

import android.content.Context;
import android.support.v4.app.NotificationCompat;

class DefaultNotificationBuilder implements NotificationBuilder {

    @Override
    public void build(Context context, CastNotification castNotification, NotificationCompat.Builder builder) {
        builder.setContentTitle(castNotification.getTitle())
                .setSmallIcon(R.drawable.ic_cast_dark)
                .setAutoCancel(true);
        switch (castNotification.getState()) {
            case CastNotification.STATE_CONNECTING:
                builder.setContentText(context.getString(R.string.cast_notifications_connecting, castNotification.getDeviceName()));
                break;
            case CastNotification.STATE_NORMAL:
                builder.setContentText(castNotification.getContentText());
                break;
        }
    }
}
