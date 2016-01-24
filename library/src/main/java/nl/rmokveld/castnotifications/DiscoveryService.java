package nl.rmokveld.castnotifications;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.media.MediaRouter;

public class DiscoveryService extends BaseCastService {

    private static final String TAG = "DiscoveryService";
    private static final String ACTION_START = BuildConfig.APPLICATION_ID + ".action.START_DISCOVERY";
    private static final String ACTION_START_WAKEUP = BuildConfig.APPLICATION_ID + ".action.START_DISCOVERY_WAKEUP";
    private static final String ACTION_REMOVE_TIMEOUT = BuildConfig.APPLICATION_ID + ".action_REMOVE_TIMEOUT";

    public static void start(Context context) {
        context.startService(buildIntent(context));
    }

    static Intent buildIntent(Context context) {
        return buildIntent(context, false);
    }

    static Intent buildIntent(Context context, boolean wakeup) {
        return new Intent(context, DiscoveryService.class).setAction(wakeup ? ACTION_START_WAKEUP : ACTION_START);
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, DiscoveryService.class));
    }

    public static void removeTimeout(Context context) {
        context.startService(new Intent(context, DiscoveryService.class).setAction(ACTION_REMOVE_TIMEOUT));
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(getTAG(), "onStartCommand() called with: " + "intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        if (ACTION_START.equals(intent.getAction()) || ACTION_START_WAKEUP.equals(intent.getAction())) {
            if (!DeviceStateHelper.isWifiConnected(this)) {
                stopDiscovery();
                return START_NOT_STICKY;
            }
            mCastNotificationManager.refreshRoutes(mMediaRouter);
            if (ACTION_START_WAKEUP.equals(intent.getAction()))
                acquireWakeLocks();
            startDiscovery(false, DeviceStateHelper.isScreenTurnedOn(this) ? 10000 : 0);
        } else if (ACTION_REMOVE_TIMEOUT.equals(intent.getAction())) {
            resetTimeout(0);
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo routeInfo) {
        Log.d(getTAG(), "onRouteAdded() called with: " + "router = [" + router + "], routeInfo = [" + routeInfo + "]");
        mCastNotificationManager.getMediaRouterCallback().onRouteAdded(router, routeInfo);
    }

    @Override
    protected void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
        Log.d(getTAG(), "onRouteRemoved() called with: " + "router = [" + router + "], route = [" + route + "]");
        mCastNotificationManager.getMediaRouterCallback().onRouteRemoved(router, route);
    }

    @Override
    protected void onDiscoveryTimeout() {
        Log.d(getTAG(), "onDiscoveryTimeout() called with: " + "");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.d(getTAG(), "onDestroy() called with: " + "");
        mCastNotificationManager.setDiscoveryAlarm();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
