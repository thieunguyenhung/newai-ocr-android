package vn.newai.ocr.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import vn.newai.ocr.R;

public class LocalStorage {
    private static final String PREFERENCES_NAME = "NewAILocalStorage";

    public static final String KEY_OCR_LANG = "OCRLang";
    public static final String KEY_USER_EMAIL = "UserEmail";

    /**
     * Save a string to Shared Preferences
     *
     * @param context Activity that calls this function
     * @param key     Shared preferences key. Use this class final properties
     * @param value   String to save
     */
    public static void saveToLocal(Context context, String key, String value) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Get a string from Shared Preferences
     *
     * @param context Activity that calls this function
     * @param key     Shared preferences key. Use this class final properties
     * @return Empty string if key does not exist. Value otherwise
     */
    public static String getFromLocal(Context context, String key) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getString(key, "");
    }

    /**
     * Initial setting when start app for the first times
     *
     * @param context Activity that calls this function
     */
    public static void initializeSetting(Context context) {
        if (getFromLocal(context, KEY_OCR_LANG).isEmpty())
            saveToLocal(context, KEY_OCR_LANG, context.getString(R.string.lang_value_vie));
    }
}
