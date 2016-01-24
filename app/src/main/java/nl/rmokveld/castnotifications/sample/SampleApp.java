package nl.rmokveld.castnotifications.sample;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.media.MediaRouteSelector;
import android.util.Log;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.TransientNetworkDisconnectionException;

import org.json.JSONObject;

import nl.rmokveld.castnotifications.CastCompanionInterface;
import nl.rmokveld.castnotifications.CastNotificationManager;
import nl.rmokveld.castnotifications.DefaultNotificationBuilder;

public class SampleApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        VideoCastManager.initialize(this, new CastConfiguration.Builder("77FF5269")
                .enableAutoReconnect()
                .enableDebug()
                .enableLockScreen()
                .enableNotification()
                .enableWifiReconnection()
                .build());


        VideoCastManager instance = VideoCastManager.getInstance();
        CastNotificationManager.init(this, new CastCompanionInterface() {
            @Override
            public void loadMedia(MediaInfo media) {
                try {
                    VideoCastManager.getInstance().loadMedia(media, true, 0);
                } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public MediaRouteSelector getMediaRouteSelector() {
                return VideoCastManager.getInstance().getMediaRouteSelector();
            }

            @Override
            public boolean isApplicationConnected() {
                return VideoCastManager.getInstance().isConnected();
            }

            @Override
            public void onDeviceSelected(CastDevice device) {
                VideoCastManager.getInstance().onDeviceSelected(device);
            }
        });
        CastNotificationManager.getInstance().setLogLevel(Log.DEBUG);
        CastNotificationManager.getInstance().setCustomNotificationBuilder(new DefaultNotificationBuilder() {

            @Override
            public void build(Context context, NotificationCompat.Builder builder, int id, String title, String subtitle, @Nullable JSONObject customData, boolean castDevicesAvailable) {
                super.build(context, builder, id, title, subtitle, customData, castDevicesAvailable);
                builder.setSmallIcon(R.drawable.ic_audiotrack);
            }
        });
        instance.addVideoCastConsumer(new VideoCastConsumerImpl() {
            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
                CastNotificationManager.getInstance().onApplicationConnected();
            }
        });
    }
}
