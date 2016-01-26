package nl.rmokveld.castnotifications;

import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;

interface NotificationHelper {
    void add(CastNotification castNotification, @Nullable Map<String, String> availableRoutes);
    void addAll(List<CastNotification> castNotifications, @Nullable Map<String, String> availableRoutes);
    void postNotifications(@Nullable Map<String, String> availableRoutes);
    boolean hasActiveNotifications();
    void cancel(int notificationId);
}
