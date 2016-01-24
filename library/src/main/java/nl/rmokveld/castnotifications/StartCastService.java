package nl.rmokveld.castnotifications;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.app.NotificationCompat;
import android.support.v7.media.MediaRouter;
import android.util.Log;

import com.google.android.gms.cast.CastDevice;

public class StartCastService extends BaseCastService implements CastNotificationManager.OnApplicationConnectedListener, CastNotificationManager.OnRouteAddedListener {

    private static final String EXTRA_NOTIFICATION = "notification_id";
    private static final String EXTRA_DEVICE_ID = "device_id";
    private static final String EXTRA_DEVICE_NAME = "device_name";
    private static final String TAG = "StartCastService";

    private RequestedDevice mRequestedDevice;
    private CastNotification mCastNotification;
    private NotificationCompat.Builder mNotificationBuilder;

    @NonNull
    public static Intent getIntent(CastNotification castNotification, Context context, String routeId, String deviceName) {
        return new Intent(context, StartCastService.class)
                .putExtra(EXTRA_NOTIFICATION, castNotification)
                .putExtra(EXTRA_DEVICE_ID, routeId)
                .putExtra(EXTRA_DEVICE_NAME, deviceName);
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mCastNotificationManager.addOnApplicationConnectedListener(this);
        mNotificationBuilder = new NotificationCompat.Builder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(getTAG(), "onStartCommand() called with: " + "intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        mRequestedDevice = new RequestedDevice(intent.getStringExtra(EXTRA_DEVICE_ID), intent.getStringExtra(EXTRA_DEVICE_NAME));
        mCastNotification = intent.getParcelableExtra(EXTRA_NOTIFICATION);
        mCastNotification.setState(CastNotification.STATE_CONNECTING, mRequestedDevice.getName());
        mCastNotificationManager.cancel(mCastNotification.getId());
        mCastNotificationManager.getNotificationBuilder().build(this, mCastNotification, mNotificationBuilder);

        acquireWakeLocks();
        startForeground(mCastNotification.getId(), mNotificationBuilder.build());

        findCastDevice();

        return START_NOT_STICKY;
    }

    @UiThread
    private void findCastDevice() {
        Log.d(getTAG(), "findCastDevice() called with: " + "");
        for (MediaRouter.RouteInfo routeInfo : mMediaRouter.getRoutes()) {
            if (mRequestedDevice.getId().equals(routeInfo.getId())) {
                mRequestedDevice.setRouteInfo(routeInfo);
            }
        }
        if (mRequestedDevice.isRouteFound())
            startCastApplication();
        else {
            startDiscovery(true, 10000);
        }
    }

    @Override
    protected void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo routeInfo) {
        Log.d(getTAG(), "onRouteAdded() called with: " + "router = [" + router + "], routeInfo = [" + routeInfo + "]");
        if (mRequestedDevice.getId().equals(routeInfo.getId())) {
            mRequestedDevice.setRouteInfo(routeInfo);
            startCastApplication();
        }
    }

    @UiThread
    private void startCastApplication() {
        Log.d(getTAG(), "startCastApplication() called");

        if (!mRequestedDevice.getRouteInfo().isSelected()) {
            mMediaRouter.addCallback(mCastNotificationManager.getMediaRouteSelector(), new MediaRouter.Callback() {
                @Override
                public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
                    if (mRequestedDevice.getId().equals(route.getId())) {
                        mCastNotificationManager.getCastCompanionInterface().onDeviceSelected(mRequestedDevice.getCastDevice());
                    }
                    mMediaRouter.removeCallback(this);
                }
            });
            mRequestedDevice.getRouteInfo().select();
        } else {
            if (!mCastNotificationManager.getCastCompanionInterface().isApplicationConnected()) {
                Log.d(getTAG(), "onDeviceSelected called on CastCompanionInterface");
                mCastNotificationManager.getCastCompanionInterface().onDeviceSelected(mRequestedDevice.getCastDevice());
            } else {
                Log.d(getTAG(), "Receiver app already connected, ready to start casting");
                onApplicationConnected();
            }
        }
    }

    @Override
    public void onApplicationConnected() {
        Log.d(getTAG(), "onApplicationConnected() called with: " + "");
        mCastNotificationManager.cancel(mCastNotification.getId());
        mCastNotificationManager.getCastCompanionInterface().loadMedia(mCastNotification.getMediaInfo());
        mRequestedDevice = null;
        stopDiscovery();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(getTAG(), "onDestroy() called with: " + "");
        super.onDestroy();
        mCastNotificationManager.removeOnApplicationConnectedListener(this);
    }

    private static class RequestedDevice {
        private final String mId;
        private final String mName;
        private MediaRouter.RouteInfo mRouteInfo;

        public RequestedDevice(String id, String name) {
            mId = id;
            mName = name;
        }

        public String getId() {
            return mId;
        }

        public String getName() {
            return mName;
        }

        public void setRouteInfo(MediaRouter.RouteInfo routeInfo) {
            mRouteInfo = routeInfo;
        }

        public MediaRouter.RouteInfo getRouteInfo() {
            return mRouteInfo;
        }

        public boolean isRouteFound() {
            return mRouteInfo != null;
        }

        public CastDevice getCastDevice() {
            if (mRouteInfo == null) return null;
            return CastDevice.getFromBundle(mRouteInfo.getExtras());
        }
    }
}
