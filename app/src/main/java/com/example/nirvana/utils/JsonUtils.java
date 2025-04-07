package com.example.nirvana.utils;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JsonUtils {
    private static final String TAG = "JsonUtils";

    /**
     * Read a JSON file from assets folder
     * @param context Application context
     * @param fileName Name of the JSON file in assets
     * @return String content of the JSON file or null if error
     */
    public static String loadJSONFromAsset(Context context, String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Log.e(TAG, "Error loading JSON: " + fileName, ex);
            return null;
        }
        return json;
    }
} 