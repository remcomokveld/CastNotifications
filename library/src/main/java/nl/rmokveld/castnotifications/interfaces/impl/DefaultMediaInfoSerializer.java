package nl.rmokveld.castnotifications.interfaces.impl;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Iterator;

import nl.rmokveld.castnotifications.interfaces.MediaInfoSerializer;

public class DefaultMediaInfoSerializer implements MediaInfoSerializer {
    @Override
    public MediaInfo toMediaInfo(String json) {
        try {
            return toMediaInfo(new JSONObject(json));
        } catch (JSONException e) {
            throw new RuntimeException(e);
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

            Iterator<String> keys = metaData.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if ("images".equals(key) || "mediaType".equals(key)) continue;
                Object value = metaData.get(key);
                if (value instanceof String)
                    mediaMetadata.putString(key, (String) value);
                else if (value instanceof Integer) {
                    mediaMetadata.putInt(key, (Integer) value);
                } else if (value instanceof Double) {
                    mediaMetadata.putDouble(key, (Double) value);
                } else if (value instanceof Long) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis((Long) value);
                    mediaMetadata.putDate(key, calendar);
                }
            }

            JSONArray images = metaData.getJSONArray("images");
            for (int i = 0; i < images.length(); i++) {
                JSONObject webImageJSON = images.getJSONObject(i);
                mediaMetadata.addImage(new WebImage(Uri.parse(webImageJSON.getString("url")), webImageJSON.getInt("width"), webImageJSON.getInt("height")));
            }
        }
        return mediaMetadata;
    }

    @Override
    public String toJson(MediaInfo mediaInfo) {
        try {
            return toJSONObject(mediaInfo).toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    protected JSONObject toJSONObject(MediaInfo mediaInfo) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("metaData", toJSONObject(mediaInfo.getMetadata()));
        jsonObject.put("contentId", mediaInfo.getContentId());
        jsonObject.put("streamType", mediaInfo.getStreamType());
        jsonObject.put("contentType", mediaInfo.getContentType());
        if (mediaInfo.getStreamDuration() != -1)
            jsonObject.put("streamDuration", mediaInfo.getStreamDuration());
        return jsonObject;
    }

    @NonNull
    protected JSONObject toJSONObject(MediaMetadata metadata) throws JSONException {
        JSONObject metaData = new JSONObject();
        if (metadata != null) {
            metaData.put("mediaType", metadata.getMediaType());
            for (String key : metadata.keySet()) {
                try {
                    metaData.put(key, metadata.getString(key));
                    continue;
                } catch (IllegalArgumentException ignored) {}
                try {
                    metaData.put(key, metadata.getInt(key));
                    continue;
                } catch (IllegalArgumentException ignored) {}
                try {
                    metaData.put(key, metadata.getDouble(key));
                    continue;
                } catch (IllegalArgumentException ignored) {}
                try {
                    metaData.put(key, metadata.getDate(key).getTimeInMillis());
                } catch (IllegalArgumentException ignored) {}
            }
            JSONArray images = new JSONArray();
            for (WebImage webImage : metadata.getImages()) {
                JSONObject webImageJSON = new JSONObject();
                webImageJSON.put("url", webImage.getUrl().toString());
                webImageJSON.put("width", webImage.getWidth());
                webImageJSON.put("height", webImage.getHeight());
                images.put(webImageJSON);
            }
            metaData.put("images", images);
        }
        return metaData;
    }
}
