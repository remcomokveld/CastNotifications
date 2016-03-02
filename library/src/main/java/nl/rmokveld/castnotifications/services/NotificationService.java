package nl.rmokveld.castnotifications.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import nl.rmokveld.castnotifications.CastNotificationManager;
import nl.rmokveld.castnotifications.data.model.CastNotification;
import nl.rmokveld.castnotifications.interfaces.NotificationHelper;
import nl.rmokveld.castnotifications.interfaces.impl.NotificationHelperImpl;

public class NotificationService extends IntentService {

    private NotificationHelper notificationHelper;

    public NotificationService() {
        super("CastNotificationService");
    }

    public static void update(Context context) {
        context.startService(new Intent(context, NotificationService.class));
    }

    public static void addNotification(Context context, CastNotification notification) {
        context.startService(new Intent(context, NotificationService.class)
                .putExtra("notification", notification));
    }

    public static void cancel(Context context, int notificationId) {
        context.startService(new Intent(context, NotificationService.class)
                .putExtra("notification_id", notificationId));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationHelper = new NotificationHelperImpl(this, CastNotificationManager.getInstance().getDiscoveryStrategy());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.hasExtra("notification")) {
            notificationHelper.notify(intent.<CastNotification>getParcelableExtra("notification"));
        } else if (intent.hasExtra("notification_id")) {
            notificationHelper.cancel(intent.getIntExtra("notification_id", 0));
        } else {
            notificationHelper.updateNotifications();
        }
    }
}
