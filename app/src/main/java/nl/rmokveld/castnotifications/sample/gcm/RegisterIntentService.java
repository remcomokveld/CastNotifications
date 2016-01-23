package nl.rmokveld.castnotifications.sample.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import nl.rmokveld.castnotifications.sample.R;

public class RegisterIntentService extends IntentService {

    private static final String TAG = "RegisterIntentService";

    public RegisterIntentService() {
        super("RegisterIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String token = InstanceID.getInstance(this).getToken(getString(R.string.gcm_sender_id), GoogleCloudMessaging.INSTANCE_ID_SCOPE);
            Log.d(TAG, token);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("gcm_token").putExtra("token", token));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
