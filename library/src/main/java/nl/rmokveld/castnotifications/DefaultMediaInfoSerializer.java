package nl.rmokveld.castnotifications;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;

import org.json.JSONException;
import org.json.JSONObject;

public class DefaultMediaInfoSerializer implements MediaInfoSerializer {
    @Override
    public MediaInfo toMediaInfo(String json) {
        try {
            return toMediaInfo(new JSONObject(json));
        } catch (JSONException e) {
            return null;
        }
    }

    @NonNull
    protected MediaInfo toMediaInfo(JSONObject jsonObject) throws JSONException {
        JSONObject metaData = jsonObject.getJSONObject("metaData");
        MediaMetadata mediaMetadata = getMediaMetadata(metaData);
        MediaInfo.Builder builder = new MediaInfo.Builder(jsonObject.getString("contentId"))
                .setStreamType(jsonObject.getInt("streamType"))
                .setContentType(jsonObject.getString("contentType"));
        if (mediaMetadata != null)
            builder.setMetadata(mediaMetadata);
        if (jsonObject.has("streamDuration"))
            builder.setStreamDuration(jsonObject.getLong("streamDuration"));
        return builder.build();
    }

    @Nullable
    protected MediaMetadata getMediaMetadata(JSONObject metaData) throws JSONException {
        MediaMetadata mediaMetadata = null;
        if (metaData != null) {
            mediaMetadata = new MediaMetadata(metaData.getInt("mediaType"));
            mediaMetadata.putString(MediaMetadata.KEY_TITLE, metaData.getString("title"));
        }
        return mediaMetadata;
    }

    @Override
    public String toJson(MediaInfo mediaInfo) {
        try {
            return toJsonObject(mediaInfo).toString();
        } catch (JSONException e) {
            return null;
        }
    }

    @NonNull
    protected JSONObject toJsonObject(MediaInfo mediaInfo) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("metaData", getMetaDataJSONObject(mediaInfo.getMetadata()));
        jsonObject.put("contentId", mediaInfo.getContentId());
        jsonObject.put("streamType", mediaInfo.getStreamType());
        jsonObject.put("contentType", mediaInfo.getContentType());
        if (mediaInfo.getStreamDuration() != -1)
            jsonObject.put("streamDuration", mediaInfo.getStreamDuration());
        return jsonObject;
    }

    @NonNull
    protected JSONObject getMetaDataJSONObject(MediaMetadata metadata) throws JSONException {
        JSONObject metaData = new JSONObject();
        if (metadata != null) {
            metaData.put("mediaType", metadata.getMediaType());
            metaData.put("title", metadata.getString(MediaMetadata.KEY_TITLE));
        }
        return metaData;
    }
}
