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
    private static final String ACTION_SET_TIMEOUT = BuildConfig.APPLICATION_ID + ".action_SET_TIMEOUT";

    public static void start(Context context, String tag) {
        context.startService(buildIntent(context, tag));
    }

    static Intent buildIntent(Context context, String tag) {
        return buildIntent(context, false, tag);
    }

    static Intent buildIntent(Context context, boolean wakeup, String tag) {
        return new Intent(context, DiscoveryService.class).setAction(wakeup ? ACTION_START_WAKEUP : ACTION_START)
                .putExtra("tag", tag);
    }

    public static void stop(Context context) {
        Log.d(TAG, "stop() called with: " + "context = [" + context + "]");
        context.stopService(new Intent(context, DiscoveryService.class));
    }

    public static void removeTimeout(Context context) {
        Log.d(TAG, "removeTimeout() called with: " + "context = [" + context + "]");
        context.startService(new Intent(context, DiscoveryService.class).setAction(ACTION_REMOVE_TIMEOUT));
    }

    public static void setTimeout(Context context) {
        Log.d(TAG, "setTimeout() called with: " + "context = [" + context + "]");
        context.startService(new Intent(context, DiscoveryService.class).setAction(ACTION_SET_TIMEOUT));
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(getTAG(), "onStartCommand() called with: " + "tag = [" + intent.getStringExtra("tag") + "], intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        if (ACTION_START.equals(intent.getAction()) || ACTION_START_WAKEUP.equals(intent.getAction())) {
            if (!DeviceStateHelper.isWifiConnected(this)) {
                stopDiscovery();
                return START_NOT_STICKY;
            }
            if (ACTION_START_WAKEUP.equals(intent.getAction()))
                acquireWakeLocks();
            startDiscovery(false, DeviceStateHelper.isScreenTurnedOn(this) ? 10000 : 0);
        } else if (ACTION_REMOVE_TIMEOUT.equals(intent.getAction())) {
            resetTimeout(0);
        } else if (ACTION_SET_TIMEOUT.equals(intent.getAction())) {
            resetTimeout(10000);
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onDiscoveryTimeout() {
        Log.d(getTAG(), "onDiscoveryTimeout() called with: " + "");
        mCastNotificationManager.getDiscoveryStrategy().onBackgroundDiscoveryStopped();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
