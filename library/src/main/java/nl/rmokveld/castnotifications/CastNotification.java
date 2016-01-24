package nl.rmokveld.castnotifications;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.google.android.gms.cast.MediaInfo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CastNotification implements Parcelable {

    public static final int STATE_NORMAL = 0;
    public static final int STATE_CONNECTING = 1;
    public static final String TABLE_NAME = "Notifications";
    static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_TEXT = "text";
    private static final String COL_MEDIA_INFO = "media_info";
    static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COL_ID + " INTEGER PRIMARY KEY," +
            COL_TITLE + " TEXT," +
            COL_TEXT + " TEXT," +
            COL_MEDIA_INFO + " TEXT);";

    private final int mId;
    private final String mTitle, mContentText;
    private final MediaInfo mMediaInfo;
    @State private int mState = STATE_NORMAL;
    private String mDeviceName;

    public CastNotification(int id, String title, String contentText, @NonNull MediaInfo mediaInfo) {
        mId = id;
        mTitle = title;
        mContentText = contentText;
        mMediaInfo = mediaInfo;
    }
    public CastNotification(Cursor cursor) {
        mId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
        mTitle = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
        mContentText = cursor.getString(cursor.getColumnIndexOrThrow(COL_TEXT));
        mMediaInfo = CastNotificationManager.getInstance().getMediaInfoSerializer().toMediaInfo(cursor.getString(cursor.getColumnIndexOrThrow(COL_MEDIA_INFO)));
    }

    protected CastNotification(Parcel in) {
        mId = in.readInt();
        mTitle = in.readString();
        mContentText = in.readString();
        //noinspection ResourceType
        mState = in.readInt();
        mDeviceName = in.readString();
        mMediaInfo = CastNotificationManager.getInstance().getMediaInfoSerializer().toMediaInfo(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mTitle);
        dest.writeString(mContentText);
        dest.writeInt(mState);
        dest.writeString(mDeviceName);
        dest.writeString(CastNotificationManager.getInstance().getMediaInfoSerializer().toJson(mMediaInfo));
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

    public int getId() {
        return mId;
    }

    public void setState(@State int state, String deviceName) {
        mState = state;
        mDeviceName = deviceName;
    }

    @State
    public int getState() {
        return mState;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ID, mId);
        contentValues.put(COL_TITLE, mTitle);
        contentValues.put(COL_TEXT, mContentText);
        contentValues.put(COL_MEDIA_INFO, CastNotificationManager.getInstance().getMediaInfoSerializer().toJson(mMediaInfo));
        return contentValues;
    }

    @IntDef({STATE_NORMAL, STATE_CONNECTING})
    @Retention(RetentionPolicy.SOURCE)
    @interface State {
    }
}
