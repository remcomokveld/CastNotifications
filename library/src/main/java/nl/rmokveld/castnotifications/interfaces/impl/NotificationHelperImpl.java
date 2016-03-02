package nl.rmokveld.castnotifications.interfaces.impl;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import java.util.List;
import java.util.Map;

import nl.rmokveld.castnotifications.BuildConfig;
import nl.rmokveld.castnotifications.CastNotificationManager;
import nl.rmokveld.castnotifications.R;
import nl.rmokveld.castnotifications.data.db.NotificationDatabase;
import nl.rmokveld.castnotifications.data.model.CastNotification;
import nl.rmokveld.castnotifications.interfaces.DiscoveryStrategy;
import nl.rmokveld.castnotifications.interfaces.NotificationHelper;
import nl.rmokveld.castnotifications.receivers.NotificationDeletedReceiver;
import nl.rmokveld.castnotifications.services.NotificationService;
import nl.rmokveld.castnotifications.services.StartCastService;
import nl.rmokveld.castnotifications.utils.Log;

@WorkerThread
public class NotificationHelperImpl implements NotificationHelper {

    private static final String TAG = "NotificationHelper";

    private final Context mContext;
    private DiscoveryStrategy discoveryStrategy;
    private NotificationDatabase database;

    public NotificationHelperImpl(Context context, DiscoveryStrategy discoveryStrategy) {
        mContext = context.getApplicationContext();
        database = new NotificationDatabase(mContext);
        this.discoveryStrategy = discoveryStrategy;
    }

    @Override
    public void updateNotifications() {
        List<CastNotification> castNotifications = database.getCastNotifications();
        Map<String, String> availableRoutes = discoveryStrategy.getAvailableRoutes();
        for (CastNotification castNotification : castNotifications) {
            notify(castNotification, availableRoutes);
        }
    }

    @Override
    public void notify(CastNotification castNotification) {
        notify(castNotification, discoveryStrategy.getAvailableRoutes());
        discoveryStrategy.setHasActiveNotifications(true);
    }

    private void notify(CastNotification notification, @Nullable Map<String, String> castDevices) {
        Log.d(TAG, "notify() called with: " + "notification = [" + notification + "], castDevices = [" + castDevices + "]");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        CastNotificationManager.getInstance().getNotificationBuilder().build(mContext, builder,
                notification.getId(), notification.getTitle(), notification.getContentText(),
                notification.getTimestamp(), notification.getCustomData(),
                castDevices != null && castDevices.size() > 0);
        builder.setDeleteIntent(PendingIntent.getBroadcast(mContext, 0,
                new Intent(mContext, NotificationDeletedReceiver.class)
                        .setData(Uri.parse("content://" + BuildConfig.APPLICATION_ID + "/notifications/" + notification.getId())),
                PendingIntent.FLAG_UPDATE_CURRENT));
        if (castDevices != null) {
            for (String routeId : castDevices.keySet()) {
                builder.addAction(R.drawable.ic_cast_light, castDevices.get(routeId),
                        PendingIntent.getService(mContext, notification.getId(), StartCastService.getIntent(notification, mContext, routeId, castDevices.get(routeId)),
                                PendingIntent.FLAG_UPDATE_CURRENT));
            }
        }
        NotificationManagerCompat.from(mContext).notify("cast_notifications", notification.getId(), builder.build());
    }

    @Override
    public void cancel(int notificationId) {
        NotificationManagerCompat.from(mContext).cancel("cast_notifications", notificationId);
        database.delete(notificationId);
        discoveryStrategy.setHasActiveNotifications(!database.getCastNotifications().isEmpty());
    }
}
