package nl.rmokveld.castnotifications;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.app.NotificationCompat;
import android.support.v7.media.MediaRouter;
import android.util.Log;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;

public class StartCastService extends BaseCastService implements CastNotificationManager.OnApplicationConnectedListener, CastNotificationManager.OnRouteAddedListener {

    private static final String EXTRA_NOTIFICATION = "notification_id";
    private static final String EXTRA_DEVICE_ID = "device_id";
    private static final String EXTRA_DEVICE_NAME = "device_name";
    private static final String TAG = "StartCastService";

    private String mRequestedDeviceId, mRequestedDeviceName;
    private MediaRouter.RouteInfo mSelectedRouteInfo;
    private CastDevice mSelectedCastDevice;
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
    public void onCreate() {
        super.onCreate();
        mCastNotificationManager.addOnApplicationConnectedListener(this);
        mNotificationBuilder = new NotificationCompat.Builder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() called with: " + "intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        mRequestedDeviceId = intent.getStringExtra(EXTRA_DEVICE_ID);
        mRequestedDeviceName = intent.getStringExtra(EXTRA_DEVICE_NAME);
        mCastNotification = intent.getParcelableExtra(EXTRA_NOTIFICATION);
        mCastNotification.setState(CastNotification.STATE_CONNECTING, mRequestedDeviceName);
        mCastNotificationManager.cancel(mCastNotification.getId());
        mCastNotificationManager.getNotificationBuilder().build(this, mCastNotification, mNotificationBuilder);

        acquireWakeLocks();
        startForeground(mCastNotification.getId(), mNotificationBuilder.build());

        findCastDevice();

        return START_NOT_STICKY;
    }

    @UiThread
    private void findCastDevice() {
        Log.d(TAG, "findCastDevice() called with: " + "");
        for (MediaRouter.RouteInfo routeInfo : mMediaRouter.getRoutes()) {
            if (mRequestedDeviceId.equals(routeInfo.getId())) {
                mSelectedRouteInfo = routeInfo;
                mSelectedCastDevice = CastDevice.getFromBundle(routeInfo.getExtras());
            }
        }
        if (mSelectedCastDevice != null)
            startCastApplication();
        else {
            startDiscovery(true, 10000);
        }
    }

    @Override
    protected void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo routeInfo) {
        Log.d(TAG, "onRouteAdded() called with: " + "router = [" + router + "], routeInfo = [" + routeInfo + "]");
        if (mRequestedDeviceId != null && mRequestedDeviceId.equals(routeInfo.getId())) {
            startCastApplication();
        }
    }

    @UiThread
    private void startCastApplication() {
        Log.d(TAG, "startCastApplication() called with: " + "");
        mRequestedDeviceId = null;
        mRequestedDeviceName = null;
        if (!mSelectedRouteInfo.isSelected()) {
            mSelectedRouteInfo.select();
        } else {
            if (!mCastNotificationManager.getCastCompanionInterface().isApplicationConnected()) {
                Log.d(TAG, "onDeviceSelected called on CastCompanionInterface");
                mCastNotificationManager.getCastCompanionInterface().onDeviceSelected(mSelectedCastDevice);
            } else {
                Log.d(TAG, "Receiver app already connected, ready to start casting");
                onApplicationConnected();
            }
        }
    }

    @Override
    public void onApplicationConnected() {
        Log.d(TAG, "onApplicationConnected() called with: " + "");
        mCastNotificationManager.cancel(mCastNotification.getId());
        mCastNotificationManager.getCastCompanionInterface().loadMedia(mCastNotification.getMediaInfo());
        mSelectedRouteInfo = null;
        mSelectedCastDevice = null;
        releaseWakeLocks();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called with: " + "");
        super.onDestroy();
        mCastNotificationManager.removeOnApplicationConnectedListener(this);
    }
}
