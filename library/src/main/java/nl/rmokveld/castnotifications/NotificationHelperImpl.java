package nl.rmokveld.castnotifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.SparseArray;

import java.util.List;
import java.util.Map;

class NotificationHelperImpl implements NotificationHelper {

    private static final String TAG = "NotificationHelper";

    private final Context mContext;
    private final SparseArray<CastNotification> mCastNotifications = new SparseArray<>();

    public NotificationHelperImpl(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void add(CastNotification castNotification, @Nullable Map<String, String> availableRoutes) {
        mCastNotifications.put(castNotification.getId(), castNotification);
        postNotification(castNotification, availableRoutes);
    }

    @Override
    public void addAll(List<CastNotification> castNotifications, @Nullable Map<String, String> availableRoutes) {
        for (CastNotification castNotification : castNotifications) {
            mCastNotifications.put(castNotification.getId(), castNotification);
        }
        postNotifications(availableRoutes);
    }

    @Override
    public void postNotification(CastNotification castNotification, @Nullable Map<String, String> castDevices) {
        Log.d(TAG, "postNotification() called with: " + "castNotification = [" + castNotification + "], castDevices = [" + castDevices + "]");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        CastNotificationManager.getInstance().getNotificationBuilder().build(mContext, builder, castNotification.getId(), castNotification.getTitle(), castNotification.getContentText(), castNotification.getTimestamp(), castNotification.getCustomData(), castDevices != null && castDevices.size() > 0);
        builder.setDeleteIntent(PendingIntent.getBroadcast(mContext, 0, new Intent(mContext, NotificationDeletedReceiver.class).setData(Uri.parse("content://" + BuildConfig.APPLICATION_ID + "/notifications/" + castNotification.getId())), PendingIntent.FLAG_UPDATE_CURRENT));
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
        for (int i = 0; i < mCastNotifications.size(); i++) {
            postNotification(mCastNotifications.valueAt(i), availableRoutes);
        }
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
}
