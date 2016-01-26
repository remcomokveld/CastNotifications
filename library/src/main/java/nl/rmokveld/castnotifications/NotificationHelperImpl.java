package nl.rmokveld.castnotifications;

import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat;
import android.util.SparseArray;

import java.util.List;
import java.util.Map;

class NotificationHelperImpl implements NotificationHelper {

    private static final String TAG = "NotificationHelper";

    private final Context mContext;
    private final SparseArray<CastNotification> mCastNotifications = new SparseArray<>();
    private final NotificationChangeHelper mNotificationChangeHelper = new NotificationChangeHelper();


    public NotificationHelperImpl(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void add(CastNotification castNotification, @Nullable Map<String, String> availableRoutes) {
        mNotificationChangeHelper.setDevices(availableRoutes);
        mCastNotifications.put(castNotification.getId(), castNotification);
        mNotificationChangeHelper.notifyNotificationsChanged();
        postNotification(castNotification, availableRoutes);
        mNotificationChangeHelper.reset();
    }

    @Override
    public void addAll(List<CastNotification> castNotifications, @Nullable Map<String, String> availableRoutes) {
        mNotificationChangeHelper.setDevices(availableRoutes);
        for (CastNotification castNotification : castNotifications) {
            mCastNotifications.put(castNotification.getId(), castNotification);
        }
        mNotificationChangeHelper.notifyNotificationsChanged();
        postNotifications(availableRoutes);
        mNotificationChangeHelper.reset();
    }

    private void postNotification(CastNotification castNotification, @Nullable Map<String, String> castDevices) {
        if (!mNotificationChangeHelper.shouldPostNotifications()) return;
        Log.d(TAG, "postNotification() called with: " + "castNotification = [" + castNotification + "], castDevices = [" + castDevices + "]");
        NotificationCompat.Builder builder = new NotificationCompatBuilder(mContext, castNotification.getId());
        CastNotificationManager.getInstance().getNotificationBuildCallback().onBuild(mContext, builder, castNotification.getId(), castNotification.getTitle(), castNotification.getContentText(), castNotification.getTimestamp(), castNotification.getCustomData(), castDevices != null && castDevices.size() > 0);
        builder.setDeleteIntent(PendingIntent.getBroadcast(mContext, castNotification.getId(), NotificationDeletedReceiver.getDeleteIntent(mContext, castNotification.getId()), PendingIntent.FLAG_UPDATE_CURRENT));
        if (castDevices != null) {
            for (String routeId : castDevices.keySet()) {
                builder.addAction(R.drawable.ic_cast_light, castDevices.get(routeId),
                        PendingIntent.getService(mContext, castNotification.getId(), StartCastService.getIntent(castNotification, mContext, routeId, castDevices.get(routeId)),
                                PendingIntent.FLAG_UPDATE_CURRENT));
            }
        }
        NotificationManagerCompat.from(mContext).notify("cast_notifications", castNotification.getId(), builder.build());
    }

    @Override
    public void postNotifications(Map<String, String> availableRoutes) {
        mNotificationChangeHelper.setDevices(availableRoutes);
        for (int i = 0; i < mCastNotifications.size(); i++) {
            postNotification(mCastNotifications.valueAt(i), availableRoutes);
        }
        mNotificationChangeHelper.reset();
    }

    @Override
    public boolean hasActiveNotifications() {
        return mCastNotifications.size() > 0;
    }

    @Override
    public void cancel(int notificationId) {
        NotificationManagerCompat.from(mContext).cancel("cast_notifications", notificationId);
        mCastNotifications.delete(notificationId);
    }

    private static class NotificationChangeHelper {
        private boolean mNotificationDirty = false;
        private Map<String, String> mLastKnownDevices;

        void notifyNotificationsChanged() {
            mNotificationDirty = true;
        }

        public void reset() {
            mNotificationDirty = false;
        }

        void setDevices(Map<String, String> devices) {
            try {
                if (mNotificationDirty || (devices == null && mLastKnownDevices == null)) return;
                if (mLastKnownDevices == null || devices == null || mLastKnownDevices.size() != devices.size()) {
                    mNotificationDirty = true;
                } else {
                    for (String s : mLastKnownDevices.keySet()) {
                        if (!devices.keySet().contains(s)) {
                            mNotificationDirty = true;
                            return;
                        }
                    }
                }
            } finally {
                mLastKnownDevices = devices;
            }
        }

        boolean shouldPostNotifications() {
            return mNotificationDirty;
        }
    }
}
