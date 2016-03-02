package nl.rmokveld.castnotifications.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import nl.rmokveld.castnotifications.BuildConfig;
import nl.rmokveld.castnotifications.utils.DeviceStateHelper;
import nl.rmokveld.castnotifications.utils.Log;

public class DiscoveryService extends BaseCastService {

    private static final String TAG = "DiscoveryService";
    private static final String ACTION_START = BuildConfig.APPLICATION_ID + ".action.START_DISCOVERY";
    private static final String ACTION_START_WAKEUP = BuildConfig.APPLICATION_ID + ".action.START_DISCOVERY_WAKEUP";
    private static final String ACTION_SET_TIMEOUT = BuildConfig.APPLICATION_ID + ".action_SET_TIMEOUT";
    private BroadcastReceiver mTimeoutBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            resetTimeout(intent.getLongExtra("timeout", 0));
        }
    };

    public static void start(Context context, String tag) {
        context.startService(buildIntent(context, false, tag));
    }

    public static Intent buildIntent(Context context, boolean wakeup, String tag) {
        return new Intent(context, DiscoveryService.class).setAction(wakeup ? ACTION_START_WAKEUP : ACTION_START)
                .putExtra("tag", tag);
    }

    public static void stop(Context context) {
        Log.d(TAG, "stop() called with: " + "context = [" + context + "]");
        context.stopService(new Intent(context, DiscoveryService.class));
    }

    public static void removeTimeout(Context context) {
        Log.d(TAG, "removeTimeout() called with: " + "context = [" + context + "]");
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_SET_TIMEOUT).putExtra("timeout", 0));
    }

    public static void setTimeout(Context context) {
        Log.d(TAG, "setTimeout() called with: " + "context = [" + context + "]");
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_SET_TIMEOUT).putExtra("timeout", 10000));
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(this).registerReceiver(mTimeoutBroadcastReceiver, new IntentFilter(ACTION_SET_TIMEOUT));
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
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onDiscoveryTimeout() {
        Log.d(getTAG(), "onDiscoveryTimeout() called with: " + "");
        mCastNotificationManager.getDiscoveryStrategy().onBackgroundDiscoveryTimeout();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mTimeoutBroadcastReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
