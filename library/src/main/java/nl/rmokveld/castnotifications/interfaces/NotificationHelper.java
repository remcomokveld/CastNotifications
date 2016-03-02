package nl.rmokveld.castnotifications.interfaces;

import android.support.annotation.WorkerThread;

import nl.rmokveld.castnotifications.data.model.CastNotification;

@WorkerThread
public interface NotificationHelper {
    void notify(CastNotification castNotification);
    void updateNotifications();
    void cancel(int notificationId);
}
