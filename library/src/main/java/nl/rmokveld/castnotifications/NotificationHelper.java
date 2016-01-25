package nl.rmokveld.castnotifications;

import java.util.List;
import java.util.Map;

interface NotificationHelper {
    void add(CastNotification castNotification, Map<String, String> availableRoutes);
    void addAll(List<CastNotification> castNotifications, Map<String, String> availableRoutes);
    void postNotification(CastNotification castNotification, Map<String, String> castDevices);
    void postNotifications(Map<String, String> availableRoutes);
    boolean hasActiveNotifications();
    void cancel(int notificationId);
}
