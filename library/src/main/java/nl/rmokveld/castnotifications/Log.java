package nl.rmokveld.castnotifications;

/**
 * Created by remco on 24/01/16.
 */
class Log {

    private static int sLevel = android.util.Log.WARN;

    static void setLevel(int level) {
        sLevel = level;
    }

    static void d(String tag, String message) {
        if (sLevel <= android.util.Log.DEBUG)
            android.util.Log.d(tag, message);
    }
}
