package nl.rmokveld.castnotifications;

import android.support.v7.app.NotificationCompat;

class DefaultNotificationBuilder implements NotificationBuilder {

    @Override
    public void build(CastNotification castNotification, NotificationCompat.Builder builder) {
        builder.setContentTitle(castNotification.getTitle())
                .setSmallIcon(R.drawable.ic_cast_dark)
                .setAutoCancel(true);
        switch (castNotification.getState()) {
            case CastNotification.STATE_CONNECTING:
                builder.setContentText(CastNotificationManager.getInstance().getContext().getString(R.string.cast_notifications_connecting, castNotification.getDeviceName()));
                break;
            case CastNotification.STATE_NORMAL:
                builder.setContentText(castNotification.getContentText());
                break;
        }
    }
}
