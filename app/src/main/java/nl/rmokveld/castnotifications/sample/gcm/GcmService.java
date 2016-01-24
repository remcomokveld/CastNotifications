package nl.rmokveld.castnotifications.sample.gcm;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

import nl.rmokveld.castnotifications.CastNotificationManager;
import nl.rmokveld.castnotifications.sample.MainActivity;

public class GcmService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        CastNotificationManager.getInstance().notify(data.getInt("id"), data.getString("title"), data.getString("text"), MainActivity.getSampleMediaInfo());
    }
}
