package nl.rmokveld.castnotifications;

import android.app.Service;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.media.MediaRouter;

public abstract class BaseCastService extends Service {

    private static final String TAG = "BaseCastService";

    private final Handler mTimeoutHandler = new Handler();

    protected CastNotificationManager mCastNotificationManager;

    protected MediaRouter mMediaRouter;
    private MediaRouter.Callback mCallback;
    private WifiManager.WifiLock mWifiLock;
    private PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        mCastNotificationManager = CastNotificationManager.getInstance();
        mMediaRouter = MediaRouter.getInstance(this);
        mCallback = new MediaRouter.Callback() {
            @Override
            public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
                BaseCastService.this.onRouteAdded(router, route);
            }

            @Override
            public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
                BaseCastService.this.onRouteRemoved(router, route);
            }
        };
        mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "CastNofications");
        mWifiLock = ((WifiManager) getSystemService(WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "CastNotifications");
    }

    protected void startDiscovery(boolean withWakeLock, long timeout) {
        Log.d(getTAG(), "startDiscovery() called with: " + "withWakeLock = [" + withWakeLock + "], timeout = [" + timeout + "]");
        mTimeoutHandler.removeCallbacksAndMessages(null);
        if (withWakeLock) {
            acquireWakeLocks();
        }
        mMediaRouter.addCallback(mCastNotificationManager.getMediaRouteSelector(), mCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        resetTimeout(timeout);
    }

    protected void resetTimeout(long timeout) {
        Log.d(getTAG(), "resetTimeout() called with: " + "timeout = [" + timeout + "]");
        mTimeoutHandler.removeCallbacksAndMessages(null);
        if (timeout > 0) {
            mTimeoutHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopDiscovery();
                    onDiscoveryTimeout();
                }
            }, timeout);
        }
    }

    protected void onDiscoveryTimeout() {

    }

    protected String getTAG() {
        return TAG;
    }

    protected void stopDiscovery() {
        Log.d(getTAG(), "stopDiscovery() called with: " + "");
        mMediaRouter.removeCallback(mCallback);
        mTimeoutHandler.removeCallbacksAndMessages(null);
        releaseWakeLocks();
    }

    protected void acquireWakeLocks() {
        Log.d(getTAG(), "acquireWakeLocks() called with: " + "");
        mWifiLock.acquire();
        mWakeLock.acquire();
    }

    protected void releaseWakeLocks() {
        Log.d(getTAG(), "releaseWakeLocks() called with: " + "");
        if (mWifiLock.isHeld()) mWifiLock.release();
        if (mWakeLock.isHeld()) mWakeLock.release();
    }

    @Override
    public void onDestroy() {
        Log.d(getTAG(), "onDestroy() called with: " + "");
        super.onDestroy();
        mMediaRouter.removeCallback(mCallback);
        mTimeoutHandler.removeCallbacksAndMessages(null);
        releaseWakeLocks();
    }

    protected abstract void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo routeInfo);

    protected void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
        Log.d(getTAG(), "onRouteRemoved() called with: " + "router = [" + router + "], route = [" + route + "]");
    }
}
