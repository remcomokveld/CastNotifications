package nl.rmokveld.castnotifications;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;

public interface CastCompanionInterface {
    void loadMedia(MediaInfo media);
    boolean isApplicationConnected();
    void onDeviceSelected(CastDevice device);
}
