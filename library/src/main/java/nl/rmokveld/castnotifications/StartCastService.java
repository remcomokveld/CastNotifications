package nl.rmokveld.castnotifications;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.media.MediaRouter;
import android.util.Log;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;

public class StartCastService extends Service implements ServiceConnection, CastNotificationManager.OnApplicationConnectedListener, CastNotificationManager.OnRouteAddedListener {

    private static final String EXTRA_NOTIFICATION_ID = "notification_id";
    private static final String EXTRA_DEVICE_ID = "device_id";
    private static final String EXTRA_DEVICE_NAME = "device_name";
    private static final String EXTRA_MEDIA_INFO = "media_info";
    private static final String TAG = "StartCastService";

    private CastNotificationManager mCastNotificationManager;
    private MediaRouter mMediaRouter;
    private String mRequestedDeviceId, mRequestedDeviceName;
    private MediaRouter.RouteInfo mSelectedRouteInfo;
    private CastDevice mSelectedCastDevice;
    private DiscoveryService mDiscoveryService;
    private MediaInfo mMediaInfo;
    private int mNotificationId;

    @NonNull
    public static Intent getIntent(int notificationId, Context context, CastDevice castDevice, MediaInfo mediaInfo) {
        return new Intent(context, StartCastService.class)
                .putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                .putExtra(EXTRA_DEVICE_ID, castDevice.getDeviceId())
                .putExtra(EXTRA_DEVICE_NAME, castDevice.getFriendlyName())
                .putExtra(EXTRA_MEDIA_INFO, CastNotificationManager.getInstance().getMediaInfoSerializer().toJson(mediaInfo));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaRouter = MediaRouter.getInstance(this);
        mCastNotificationManager = CastNotificationManager.getInstance();
        mCastNotificationManager.setOnApplicationConnectedListener(this);
        mCastNotificationManager.addOnRouteAddedListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() called with: " + "intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        mRequestedDeviceId = intent.getStringExtra(EXTRA_DEVICE_ID);
        mRequestedDeviceName = intent.getStringExtra(EXTRA_DEVICE_NAME);
        mMediaInfo = CastNotificationManager.getInstance().getMediaInfoSerializer().toMediaInfo(intent.getStringExtra(EXTRA_MEDIA_INFO));
        mNotificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
        mCastNotificationManager.setStateConnecting(mNotificationId, mRequestedDeviceName);
        findCastDevice();
        return START_NOT_STICKY;
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
                mCastNotificationManager.getCastCompanionInterface().onDeviceSelected(mSelectedCastDevice);
            } else {
                onApplicationConnected();
            }
        }
    }

    @Override
    public void onApplicationConnected() {
        Log.d(TAG, "onApplicationConnected() called with: " + "");
        mCastNotificationManager.cancel(mNotificationId);
        mCastNotificationManager.getCastCompanionInterface().loadMedia(mMediaInfo);
        mSelectedRouteInfo = null;
        mSelectedCastDevice = null;
        stopSelf();
    }

    @UiThread
    private void findCastDevice() {
        for (MediaRouter.RouteInfo routeInfo : mMediaRouter.getRoutes()) {
            if (routeInfo.isDefault()) continue;
            CastDevice castDevice = CastDevice.getFromBundle(routeInfo.getExtras());
            if (castDevice != null && mRequestedDeviceId.equals(castDevice.getDeviceId())) {
                mSelectedRouteInfo = routeInfo;
                mSelectedCastDevice = castDevice;
            }
        }
        if (mSelectedCastDevice != null)
            startCastApplication();
        else {
            if (mDiscoveryService == null)
                DiscoveryService.bind(this, getString(R.string.cast_notifications_connecting, mRequestedDeviceName), true, this);
            else
                mDiscoveryService.startDiscovery(true, true, getString(R.string.cast_notifications_connecting, mRequestedDeviceName));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mDiscoveryService = ((DiscoveryService.LocalBinder) service).getService();
        mDiscoveryService.startDiscovery(true, true, getString(R.string.cast_notifications_connecting, mRequestedDeviceName));
        Log.d(TAG, "Discovery active: " + mDiscoveryService.isDiscoveryActive());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mDiscoveryService = null;
    }

    @Override
    public void onRouteAdded(MediaRouter.RouteInfo routeInfo, CastDevice castDevice) {
        if (castDevice.getDeviceId().equals(mRequestedDeviceId)) {
            mSelectedRouteInfo = routeInfo;
            mSelectedCastDevice = castDevice;
            startCastApplication();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCastNotificationManager.setOnApplicationConnectedListener(null);
        mCastNotificationManager.removeOnRouteAddedListener(this);
    }
}
