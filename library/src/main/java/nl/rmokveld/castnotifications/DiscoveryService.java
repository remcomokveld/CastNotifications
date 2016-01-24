package nl.rmokveld.castnotifications;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.support.v7.media.MediaRouter;
import android.util.Log;

public class DiscoveryService extends BaseCastService {

    private static final String TAG = "DiscoveryService";
    private static final String ACTION_START = BuildConfig.APPLICATION_ID + ".action.START_DISCOVERY";
    private static final String ACTION_REMOVE_TIMEOUT = BuildConfig.APPLICATION_ID + ".action_REMOVE_TIMEOUT";

    public static void start(Context context) {
        context.startService(buildIntent(context));
    }

    static Intent buildIntent(Context context) {
        return new Intent(context, DiscoveryService.class).setAction(ACTION_START);
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, DiscoveryService.class));
    }

    public static void removeTimeout(Context context) {
        context.startService(new Intent(context, DiscoveryService.class).setAction(ACTION_REMOVE_TIMEOUT));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() called with: " + "intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        if (ACTION_START.equals(intent.getAction())) {
            if (!DeviceStateHelper.isWifiConnected(this)) {
                stopDiscovery();
                return START_NOT_STICKY;
            }
            mCastNotificationManager.refreshRoutes(mMediaRouter);
            startDiscovery(false, DeviceStateHelper.isScreenTurnedOn(this) ? 10000 : 0);
        } else if (ACTION_REMOVE_TIMEOUT.equals(intent.getAction())) {
            resetTimeout(0);
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo routeInfo) {
        Log.d(TAG, "onRouteAdded() called with: " + "router = [" + router + "], routeInfo = [" + routeInfo + "]");
        mCastNotificationManager.getMediaRouterCallback().onRouteAdded(router, routeInfo);
    }

    @Override
    protected void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
        Log.d(TAG, "onRouteRemoved() called with: " + "router = [" + router + "], route = [" + route + "]");
        mCastNotificationManager.getMediaRouterCallback().onRouteRemoved(router, route);
    }

    @Override
    protected void onDiscoveryTimeout() {
        Log.d(TAG, "onDiscoveryTimeout() called with: " + "");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called with: " + "");
        mCastNotificationManager.setDiscoveryAlarm();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
