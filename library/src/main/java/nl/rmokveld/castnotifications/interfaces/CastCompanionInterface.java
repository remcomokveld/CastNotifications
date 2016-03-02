package nl.rmokveld.castnotifications.interfaces;

import android.support.v7.media.MediaRouteSelector;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;

public interface CastCompanionInterface {
    void loadMedia(MediaInfo media);
    boolean isApplicationConnected();
    void onDeviceSelected(CastDevice device);
    MediaRouteSelector getMediaRouteSelector();
}
