package nl.rmokveld.castnotifications;

import android.app.Notification;
import android.app.PendingIntent;
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

    class Builder extends NotificationCompat.Builder {

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
         * @param id the notification id
         */
        public Builder(Context context, int id) {
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
                super.setContentIntent(PendingIntent.getBroadcast(mContext, mId, NotificationDeletedReceiver.getIntent(mContext, mId, mOriginalContentIntent, mAutoCancel), PendingIntent.FLAG_UPDATE_CURRENT));
            return super.build();
        }
    }
}
