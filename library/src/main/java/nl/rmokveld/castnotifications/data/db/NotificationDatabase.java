package nl.rmokveld.castnotifications.data.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.WorkerThread;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import nl.rmokveld.castnotifications.BuildConfig;
import nl.rmokveld.castnotifications.data.model.CastNotification;

public class NotificationDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = BuildConfig.APPLICATION_ID+".notifications.db";
    private static final int DB_VERSION = 1;
    private final Handler mDbHandler;

    public NotificationDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        HandlerThread thread = new HandlerThread("CastNotificationsDb", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mDbHandler = new Handler(thread.getLooper());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CastNotification.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @WorkerThread
    public List<CastNotification> getCastNotifications() {
        Cursor cursor = getReadableDatabase().query(CastNotification.TABLE_NAME, null, null, null, null, null, null);
        List<CastNotification> notifications = new ArrayList<>(cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                notifications.add(new CastNotification(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notifications;
    }

    public void getCastNotifications(final Callback callback) {
        final Handler callingHandler = new Handler();
        mDbHandler.post(new Runnable() {
            @Override
            public void run() {
                final List<CastNotification> castNotifications = getCastNotifications();
                callingHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onComplete(castNotifications);
                    }
                });
            }
        });
    }

    public void delete(final int notificationId) {
        mDbHandler.post(new Runnable() {
            @Override
            public void run() {
                getWritableDatabase().delete(CastNotification.TABLE_NAME,
                        CastNotification.COL_ID+"=?",
                        new String[]{String.valueOf(notificationId)});
            }
        });

    }

    public void persistNotifications(final SparseArray<CastNotification> castNotifications) {
        mDbHandler.post(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase database = getWritableDatabase();
                database.beginTransaction();
                try {
                    for (int i = 0; i < castNotifications.size(); i++) {
                        database.replace(CastNotification.TABLE_NAME, null, castNotifications.valueAt(i).toContentValues());
                    }
                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                }
            }
        });
    }

    public void persistNotification(final CastNotification notification) {
        mDbHandler.post(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase database = getWritableDatabase();
                database.replace(CastNotification.TABLE_NAME, null, notification.toContentValues());
            }
        });
    }

    public interface Callback {
        void onComplete(List<CastNotification> castNotifications);
    }
}
