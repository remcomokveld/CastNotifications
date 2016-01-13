package nl.rmokveld.castnotifications;

import com.google.android.gms.cast.MediaInfo;

public interface MediaInfoSerializer {
    MediaInfo toMediaInfo(String json);
    String toJson(MediaInfo mediaInfo);
}
