package nl.rmokveld.castnotifications.sample.gcm;

import com.google.android.gms.iid.InstanceIDListenerService;

public class InstanceIdService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
    }
}
