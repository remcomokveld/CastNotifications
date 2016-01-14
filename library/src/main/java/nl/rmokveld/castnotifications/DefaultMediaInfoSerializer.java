package nl.rmokveld.castnotifications;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;

import org.json.JSONException;
import org.json.JSONObject;

public class DefaultMediaInfoSerializer implements MediaInfoSerializer {
    @Override
    public MediaInfo toMediaInfo(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject metaData = jsonObject.getJSONObject("metaData");
            MediaMetadata mediaMetadata = new MediaMetadata(metaData.getInt("mediaType"));
            mediaMetadata.putString(MediaMetadata.KEY_TITLE, metaData.getString("title"));
            MediaInfo.Builder builder = new MediaInfo.Builder(jsonObject.getString("contentId"))
                    .setStreamType(jsonObject.getInt("streamType"))
                    .setContentType(jsonObject.getString("contentType"))
                    .setMetadata(mediaMetadata);
            if (jsonObject.has("streamDuration"))
                builder.setStreamDuration(jsonObject.getLong("streamDuration"));
            return builder.build();
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public String toJson(MediaInfo mediaInfo) {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject metaData = new JSONObject();
            if (mediaInfo.getMetadata() != null) {
                metaData.put("mediaType", mediaInfo.getMetadata().getMediaType());
                metaData.put("title", mediaInfo.getMetadata().getString(MediaMetadata.KEY_TITLE));
            }
            jsonObject.put("metaData", metaData);
            jsonObject.put("contentId", mediaInfo.getContentId());
            jsonObject.put("streamType", mediaInfo.getStreamType());
            jsonObject.put("contentType", mediaInfo.getContentType());
            if (mediaInfo.getStreamDuration() != -1)
                jsonObject.put("streamDuration", mediaInfo.getStreamDuration());
            return jsonObject.toString();
        } catch (JSONException e) {
            return null;
        }
    }
}
