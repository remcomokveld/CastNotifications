package nl.rmokveld.castnotifications;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.cast.MediaInfo;

import org.json.JSONException;
import org.json.JSONObject;

class CastNotification implements Parcelable {

    private static MediaInfoSerializer sMediaInfoSerializer = new DefaultMediaInfoSerializer();
    public static final String TABLE_NAME = "Notifications";
    static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_TEXT = "text";
    private static final String COL_TIMESTAMP = "timestamp";
    private static final String COL_MEDIA_INFO = "media_info";
    private static final String COL_CUSTOM_DATA = "custom_data";
    static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COL_ID + " INTEGER PRIMARY KEY," +
            COL_TITLE + " TEXT," +
            COL_TEXT + " TEXT," +
            COL_TIMESTAMP + " INTEGER," +
            COL_MEDIA_INFO + " TEXT," +
            COL_CUSTOM_DATA + " TEXT);";

    private final int mId;
    private final String mTitle, mContentText;
    private final long mTimestamp;
    private final MediaInfo mMediaInfo;
    private final JSONObject mCustomData;
    private String mDeviceName;

    public CastNotification(int id, String title, String contentText, long timestamp, @NonNull MediaInfo mediaInfo, @Nullable JSONObject customData) {
        mId = id;
        mTitle = title;
        mContentText = contentText;
        mTimestamp = timestamp;
        mMediaInfo = mediaInfo;
        mCustomData = customData;
    }
    public CastNotification(Cursor cursor) {
        mId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
        mTitle = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
        mContentText = cursor.getString(cursor.getColumnIndexOrThrow(COL_TEXT));
        mTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP));
        mMediaInfo = sMediaInfoSerializer.toMediaInfo(cursor.getString(cursor.getColumnIndexOrThrow(COL_MEDIA_INFO)));
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(cursor.getString(cursor.getColumnIndexOrThrow(COL_CUSTOM_DATA)));
        } catch (Exception ignored) {}
        mCustomData = jsonObject;
    }

    protected CastNotification(Parcel in) {
        mId = in.readInt();
        mTitle = in.readString();
        mContentText = in.readString();
        //noinspection ResourceType
        mTimestamp = in.readLong();
        mDeviceName = in.readString();
        mMediaInfo = sMediaInfoSerializer.toMediaInfo(in.readString());
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(in.readString());
        } catch (Exception ignored) {}
        mCustomData = jsonObject;
    }

    public static void setMediaInfoSerializer(MediaInfoSerializer mediaInfoSerializer) {
        sMediaInfoSerializer = mediaInfoSerializer;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mTitle);
        dest.writeString(mContentText);
        dest.writeLong(mTimestamp);
        dest.writeString(mDeviceName);
        dest.writeString(sMediaInfoSerializer.toJson(mMediaInfo));
        dest.writeString(mCustomData != null ? mCustomData.toString() : null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CastNotification> CREATOR = new Creator<CastNotification>() {
        @Override
        public CastNotification createFromParcel(Parcel in) {
            return new CastNotification(in);
        }

        @Override
        public CastNotification[] newArray(int size) {
            return new CastNotification[size];
        }
    };

    public String getTitle() {
        return mTitle;
    }

    public String getContentText() {
        return mContentText;
    }

    public MediaInfo getMediaInfo() {
        return mMediaInfo;
    }

    public JSONObject getCustomData() {
        return mCustomData;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public int getId() {
        return mId;
    }

    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ID, mId);
        contentValues.put(COL_TITLE, mTitle);
        contentValues.put(COL_TEXT, mContentText);
        contentValues.put(COL_MEDIA_INFO, sMediaInfoSerializer.toJson(mMediaInfo));
        if (mCustomData != null)
            contentValues.put(COL_CUSTOM_DATA, mCustomData.toString());
        return contentValues;
    }

}
