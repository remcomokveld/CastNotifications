package nl.rmokveld.castnotifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

/**
 * The NotificationCompatBuilder is an extension of the NotificationCompat.Builder class
 * which makes sure that whenever an notification's ContentIntent is set, and autoCancel
 * is set to true the original ContentIntent is wrapped in a PendingIntent for
 * NotificationDeletedReceiver so that the notification gets canceled.
 * The NotificationDeletedReceiver will then call the original PendingIntent
 */
class NotificationCompatBuilder extends NotificationCompat.Builder {

    private final int mId;
    private boolean mAutoCancel;
    private PendingIntent mOriginalContentIntent;

    /**
     * Constructor.
     * <p/>
     * Automatically sets the when field to {@link System#currentTimeMillis()
     * System.currentTimeMillis()} and the audio stream to the
     * {@link Notification#STREAM_DEFAULT}.
     *
     * @param context A {@link Context} that will be used to construct the
     *                RemoteViews. The Context will not be held past the lifetime of this
     *                Builder object.
     * @param id      the notification id
     */
    public NotificationCompatBuilder(Context context, int id) {
        super(context);
        mId = id;
    }

    @Override
    public NotificationCompat.Builder setContentIntent(PendingIntent intent) {
        mOriginalContentIntent = intent;
        return this;
    }

    @Override
    public NotificationCompat.Builder setAutoCancel(boolean autoCancel) {
        mAutoCancel = autoCancel;
        return super.setAutoCancel(autoCancel);
    }

    @Override
    public Notification build() {
        if (mOriginalContentIntent != null)
            super.setContentIntent(PendingIntent.getBroadcast(mContext, mId,
                    NotificationDeletedReceiver.getIntent(mContext, mId, mOriginalContentIntent, mAutoCancel),
                    PendingIntent.FLAG_UPDATE_CURRENT));
        return super.build();
    }
}
