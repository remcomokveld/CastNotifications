package nl.rmokveld.castnotifications.utils;

/**
 * Created by remco on 24/01/16.
 */
public class Log {

    private static int sLevel = android.util.Log.WARN;

    public static void setLevel(int level) {
        sLevel = level;
    }

    public static void d(String tag, String message) {
        if (sLevel <= android.util.Log.DEBUG)
            android.util.Log.d(tag, message);
    }

    public static void e(String tag, String message, Throwable e) {
        if (sLevel <= android.util.Log.ERROR)
            android.util.Log.e(tag, message, e);
    }
}
