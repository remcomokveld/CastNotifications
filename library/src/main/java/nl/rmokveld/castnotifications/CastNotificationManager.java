package nl.rmokveld.castnotifications;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.media.MediaRouteSelector;

import com.google.android.gms.cast.MediaInfo;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import nl.rmokveld.castnotifications.data.model.CastNotification;
import nl.rmokveld.castnotifications.interfaces.CastCompanionInterface;
import nl.rmokveld.castnotifications.interfaces.DiscoveryStrategy;
import nl.rmokveld.castnotifications.interfaces.MediaInfoSerializer;
import nl.rmokveld.castnotifications.interfaces.NotificationBuilder;
import nl.rmokveld.castnotifications.interfaces.NotificationHelper;
import nl.rmokveld.castnotifications.interfaces.impl.DefaultMediaInfoSerializer;
import nl.rmokveld.castnotifications.interfaces.impl.DefaultNotificationBuilder;
import nl.rmokveld.castnotifications.interfaces.impl.DiscoveryStrategyImpl;
import nl.rmokveld.castnotifications.interfaces.impl.NotificationHelperImpl;
import nl.rmokveld.castnotifications.services.NotificationService;
import nl.rmokveld.castnotifications.utils.Log;

public class CastNotificationManager {

    private static final String TAG = "CastNotificationManager";

    private static CastNotificationManager instance;

    private final Context context;
    private final CastCompanionInterface castCompanionInterface;
    private final NotificationBuilder notificationBuilder;
    private final MediaInfoSerializer mediaInfoSerializer;
    private final DiscoveryStrategy discoveryStrategy;
    private final NotificationHelper notificationHelper;

    private final Set<OnApplicationConnectedListener> onApplicationConnectedListeners = new HashSet<>();

    public static void init(Context context, @NonNull Config config) {
        instance = new CastNotificationManager(context, config);
    }

    public static CastNotificationManager getInstance() {
        if (instance == null) {
            throw new ExceptionInInitializerError("CastNotificationManager not initialized");
        }
        return instance;
    }

    private CastNotificationManager(Context context,
                                    @NonNull Config config) {
        this.context = context =  context.getApplicationContext();
        castCompanionInterface = config.castCompanionInterface;
        notificationBuilder = config.notificationBuilder != null ? config.notificationBuilder : new DefaultNotificationBuilder();
        mediaInfoSerializer = config.mediaInfoSerializer != null ? config.mediaInfoSerializer : new DefaultMediaInfoSerializer();
        Log.setLevel(config.logLevel);

        discoveryStrategy = new DiscoveryStrategyImpl(context);
        notificationHelper = new NotificationHelperImpl(context, discoveryStrategy);

        NotificationService.update(context);
    }

    public void notify(int id, String title, String contentText, @NonNull MediaInfo mediaInfo, @Nullable JSONObject customData) {
        Log.d(TAG, "notify() called with: " + "id = [" + id + "], title = [" + title + "], contentText = [" + contentText + "], mediaInfo = [" + mediaInfo + "]");
        CastNotification notification = new CastNotification(id, title, contentText, System.currentTimeMillis(), mediaInfo, customData);
        if (Looper.getMainLooper() == Looper.myLooper())
            NotificationService.addNotification(context, notification);
        else
            notificationHelper.notify(notification);
    }

    public void cancel(int notificationId) {
        Log.d(TAG, "cancel() called with: " + "notificationId = [" + notificationId + "]");
        if (Looper.getMainLooper() == Looper.myLooper())
            NotificationService.cancel(context, notificationId);
        else
            notificationHelper.cancel(notificationId);
    }

    @NonNull
    public MediaRouteSelector getMediaRouteSelector() {
        return castCompanionInterface.getMediaRouteSelector();
    }

    @NonNull
    public CastCompanionInterface getCastCompanionInterface() {
        return castCompanionInterface;
    }

    public NotificationBuilder getNotificationBuilder() {
        return notificationBuilder;
    }

    public MediaInfoSerializer getMediaInfoSerializer() {
        return mediaInfoSerializer;
    }

    public DiscoveryStrategy getDiscoveryStrategy() {
        return discoveryStrategy;
    }

    public Context getContext() {
        return context;
    }

    public void onApplicationConnected() {
        for (OnApplicationConnectedListener listener : onApplicationConnectedListeners) {
            listener.onApplicationConnected();
        }
    }

    public void addOnApplicationConnectedListener(OnApplicationConnectedListener listener) {
        onApplicationConnectedListeners.add(listener);
    }

    public void removeOnApplicationConnectedListener(OnApplicationConnectedListener listener) {
        onApplicationConnectedListeners.remove(listener);
    }

    public interface OnApplicationConnectedListener {
        void onApplicationConnected();
    }

    @SuppressWarnings("unused")
    public static class Config {
        CastCompanionInterface castCompanionInterface;
        NotificationBuilder notificationBuilder;
        MediaInfoSerializer mediaInfoSerializer;
        int logLevel = android.util.Log.WARN;

        public Config(@NonNull CastCompanionInterface castCompanionInterface) {
            this.castCompanionInterface = castCompanionInterface;
        }

        public Config withNotificationBuilder(NotificationBuilder notificationBuilder) {
            this.notificationBuilder = notificationBuilder;
            return this;
        }

        public Config withMediaInfoSerializer(MediaInfoSerializer mediaInfoSerializer) {
            this.mediaInfoSerializer = mediaInfoSerializer;
            return this;
        }

        public Config withLogLever(int logLevel) {
            this.logLevel = logLevel;
            return this;
        }
    }

}
