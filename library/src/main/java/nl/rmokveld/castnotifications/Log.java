package nl.rmokveld.castnotifications;

import java.util.Map;

import nl.thecapitals.firebasedevlogging.FirebaseLog;

/**
 * Created by remco on 24/01/16.
 */
class Log {

    private static int sLevel = android.util.Log.WARN;

    static void setLevel(int level) {
        sLevel = level;
    }

    static void d(String tag, String message) {
        d(tag, message, null);
    }

    static void d(String tag, String message, Map<String, Object> extraData) {
        if (sLevel <= android.util.Log.DEBUG)
            android.util.Log.d(tag, message);
        FirebaseLog.d(tag, message, extraData);
    }
}
