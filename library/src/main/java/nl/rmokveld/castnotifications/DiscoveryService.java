package nl.rmokveld.castnotifications;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.support.v7.media.MediaRouter;
import android.util.Log;

public class DiscoveryService extends Service {

    private static final String TAG = "DiscoveryService";
    private static final String ACTION_START = BuildConfig.APPLICATION_ID + ".action.START_DISCOVERY";
    private static final String ACTION_REMOVE_TIMEOUT = BuildConfig.APPLICATION_ID + ".action_REMOVE_TIMEOUT";
    private static final String EXTRA_NOTIFICATION_MESSAGE = "notification_message";
    private static final String EXTRA_WAKE_UP = "wake_up";
    private static final long TIMEOUT = 10000;

    private final LocalBinder mBinder = new LocalBinder();

    private MediaRouter mMediaRouter;

    private WifiManager.WifiLock mWifiLock;
    private PowerManager.WakeLock mPowerWakeLock;

    private Handler mTimeoutHandler;
    private final Runnable mTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            stopDiscovery();
        }
    };

    private boolean mDiscoveryActive = false;

    public static void start(Context context, String notificationMessage, boolean wakeUp) {
        context.startService(buildIntent(context, notificationMessage, wakeUp));
    }

    public static void bind(Context context, String notificationMessage, boolean wakeUp, ServiceConnection serviceConnection) {
        context.bindService(buildIntent(context, notificationMessage, wakeUp), serviceConnection, BIND_AUTO_CREATE);
    }

    static Intent buildIntent(Context context, String notificationMessage, boolean wakeUp) {
        return new Intent(context, DiscoveryService.class).setAction(ACTION_START)
                .putExtra(EXTRA_WAKE_UP, wakeUp)
                .putExtra(EXTRA_NOTIFICATION_MESSAGE, notificationMessage);
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, DiscoveryService.class));
    }

    public static void removeTimeout(Context context) {
        context.startService(new Intent(context, DiscoveryService.class).setAction(ACTION_REMOVE_TIMEOUT));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaRouter = MediaRouter.getInstance(this);
        //noinspection deprecation
        mPowerWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "test");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            mWifiLock = ((WifiManager) getSystemService(WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "CastNotifications");
        } else {
            mWifiLock = ((WifiManager) getSystemService(WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "CastNotifications");
        }
        mTimeoutHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() called with: " + "intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        if (ACTION_START.equals(intent.getAction())) {
            if (!DeviceStateHelper.isWifiConnected(this)) {
                stopSelf();
                return START_NOT_STICKY;
            }
            String notificationTitle = intent.hasExtra(EXTRA_NOTIFICATION_MESSAGE) ? intent.getStringExtra(EXTRA_NOTIFICATION_MESSAGE) : getString(R.string.cast_notifications_discovering);
            for (MediaRouter.RouteInfo routeInfo : mMediaRouter.getRoutes()) {
                CastNotificationManager.getInstance().getMediaRouterCallback().onRouteAdded(mMediaRouter, routeInfo);
            }
            startDiscovery(intent.getBooleanExtra(EXTRA_WAKE_UP, false), DeviceStateHelper.isScreenTurnedOn(this), notificationTitle);
        } else if (ACTION_REMOVE_TIMEOUT.equals(intent.getAction())) {
            mTimeoutHandler.removeCallbacks(mTimeOutRunnable);
        }
        return START_REDELIVER_INTENT;
    }

    private void startForeground(String title, String contentText) {
        Log.d(TAG, "startForeground() called with: " + "title = [" + title + "], contentText = [" + contentText + "]");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_stat_cast_notifications)
                .setContentTitle(title)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_MIN);
        startForeground(1, builder.build());
    }

    public void startDiscovery(boolean wakeUp, boolean timeout, String notificationTitle) {
        Log.d(TAG, "startDiscovery() called with: " + "wakeUp = [" + wakeUp + "], timeout = [" + timeout + "], notificationTitle = [" + notificationTitle + "]");
        mTimeoutHandler.removeCallbacks(mTimeOutRunnable);
        if (wakeUp) {
            mPowerWakeLock.acquire();
            startForeground(notificationTitle, "");
        }
        CastNotificationManager.getInstance().refreshRoutes(mMediaRouter);
        mMediaRouter.addCallback(CastNotificationManager.getInstance().getMediaRouteSelector(), CastNotificationManager.getInstance().getMediaRouterCallback(), MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        mDiscoveryActive = true;
        if (timeout) {
            mTimeoutHandler.postDelayed(mTimeOutRunnable, TIMEOUT);
        }
        Log.d(TAG, "onStartCommand: Discovery started");
    }

    private void stopDiscovery() {
        Log.d(TAG, "stopDiscovery() called with: " + "");
        mMediaRouter.removeCallback(CastNotificationManager.getInstance().getMediaRouterCallback());
        mDiscoveryActive = false;
        if (mWifiLock.isHeld()) mWifiLock.release();
        if (mPowerWakeLock.isHeld()) mPowerWakeLock.release();
        mTimeoutHandler.removeCallbacks(mTimeOutRunnable);
        CastNotificationManager.getInstance().setDiscoveryAlarm();
        stopForeground(true);
    }

    public boolean isDiscoveryActive() {
        return mDiscoveryActive;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopDiscovery();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public DiscoveryService getService() {
            return DiscoveryService.this;
        }
    }

}
