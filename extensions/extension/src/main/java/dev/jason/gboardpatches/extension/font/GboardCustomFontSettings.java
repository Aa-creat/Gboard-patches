package dev.jason.gboardpatches.extension.font;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

import dev.jason.gboardpatches.extension.settings.GboardPatchesSettings;

public final class GboardCustomFontSettings {
    private static final String TAG = "GboardCustomFont";

    public static final String KEY_ENABLED = "gboard_custom_font_enabled";
    public static final String KEY_MODE = "gboard_custom_font_mode";
    public static final String KEY_EMOJI_FILENAME = "gboard_custom_font_emoji_filename";
    public static final String KEY_TEXT_FILENAME = "gboard_custom_font_text_filename";

    public static final String MODE_EMOJI_ONLY = "emoji_only";
    public static final String MODE_GLOBAL = "global";

    public static final String EMOJI_FONT_FILE_NAME = "gboard_custom_emoji.ttf";
    public static final String TEXT_FONT_FILE_NAME = "gboard_custom_text.ttf";
    public static final String LEGACY_FONT_FILE_NAME = "gboard_custom_font.ttf";
    private static final String TEMP_FONT_FILE_NAME = "gboard_font_temp.tmp";

    private GboardCustomFontSettings() {
    }

    public static SharedPreferences preferences(Context context) {
        return GboardPatchesSettings.preferences(context);
    }

    public static File getEmojiFontFile(Context context) {
        File primary = new File(context.getFilesDir(), EMOJI_FONT_FILE_NAME);
        if (primary.isFile()) {
            return primary;
        }
        File legacy = new File(context.getFilesDir(), LEGACY_FONT_FILE_NAME);
        if (legacy.isFile()) {
            return legacy;
        }
        return primary;
    }

    public static File getTextFontFile(Context context) {
        return new File(context.getFilesDir(), TEXT_FONT_FILE_NAME);
    }

    public static boolean hasEmojiFont(Context context) {
        return getEmojiFontFile(context).isFile();
    }

    public static boolean hasTextFont(Context context) {
        return getTextFontFile(context).isFile();
    }

    public static boolean hasAnyFont(Context context) {
        return hasEmojiFont(context) || hasTextFont(context);
    }

    public static boolean readEnabled(Context context) {
        SharedPreferences prefs = preferences(context);
        return prefs.getBoolean(KEY_ENABLED, false);
    }

    public static void writeEnabled(Context context, boolean enabled) {
        preferences(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public static boolean isCustomFontActive(Context context) {
        return readEnabled(context) && hasAnyFont(context);
    }

    public static String readMode(Context context) {
        String mode = preferences(context).getString(KEY_MODE, MODE_EMOJI_ONLY);
        if (MODE_GLOBAL.equalsIgnoreCase(mode)) {
            return MODE_GLOBAL;
        }
        return MODE_EMOJI_ONLY;
    }

    public static void writeMode(Context context, String mode) {
        String validMode = MODE_GLOBAL.equalsIgnoreCase(mode) ? MODE_GLOBAL : MODE_EMOJI_ONLY;
        preferences(context).edit().putString(KEY_MODE, validMode).apply();
    }

    public static String getEmojiFontDisplayName(Context context) {
        File file = getEmojiFontFile(context);
        if (!file.isFile()) {
            return "";
        }
        String storedName = preferences(context).getString(KEY_EMOJI_FILENAME, "");
        if (storedName != null && !storedName.isBlank()) {
            return storedName.trim();
        }
        return file.getName();
    }

    public static String getTextFontDisplayName(Context context) {
        File file = getTextFontFile(context);
        if (!file.isFile()) {
            return "";
        }
        String storedName = preferences(context).getString(KEY_TEXT_FILENAME, "");
        if (storedName != null && !storedName.isBlank()) {
            return storedName.trim();
        }
        return file.getName();
    }

    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) {
            return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        }
        return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    public static boolean importEmojiFont(Context context, String displayName, byte[] data) {
        return importFontInternal(context, getEmojiFontFile(context), KEY_EMOJI_FILENAME, displayName, data);
    }

    public static boolean importTextFont(Context context, String displayName, byte[] data) {
        return importFontInternal(context, getTextFontFile(context), KEY_TEXT_FILENAME, displayName, data);
    }

    private static boolean importFontInternal(Context context, File targetFile, String prefKey,
                                              String displayName, byte[] data) {
        if (context == null || targetFile == null || data == null || data.length < 100) {
            return false;
        }

        File tempFile = new File(context.getFilesDir(), TEMP_FONT_FILE_NAME);
        try {
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(data);
                fos.flush();
            }

            Typeface parsed;
            try {
                parsed = new Typeface.Builder(tempFile).build();
            } catch (Throwable t) {
                Log.w(TAG, "Typeface.Builder rejected font file", t);
                parsed = null;
            }

            if (parsed == null) {
                Log.w(TAG, "Selected file is not a valid font or could not be loaded");
                //noinspection ResultOfMethodCallIgnored
                tempFile.delete();
                return false;
            }

            if (targetFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                targetFile.delete();
            }

            if (!tempFile.renameTo(targetFile)) {
                // Fallback copy if renameTo fails
                try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                    fos.write(data);
                    fos.flush();
                }
                //noinspection ResultOfMethodCallIgnored
                tempFile.delete();
            }

            SharedPreferences.Editor editor = preferences(context).edit();
            editor.putString(prefKey, displayName != null ? displayName : targetFile.getName());
            editor.putBoolean(KEY_ENABLED, true);
            editor.apply();

            Log.i(TAG, "Font imported successfully: " + targetFile.getName() + " (" + data.length + " bytes)");
            return true;
        } catch (Throwable throwable) {
            Log.e(TAG, "Failed to import font: " + targetFile.getName(), throwable);
            //noinspection ResultOfMethodCallIgnored
            tempFile.delete();
            return false;
        }
    }

    public static boolean clearEmojiFont(Context context) {
        File file = getEmojiFontFile(context);
        boolean deleted = !file.exists() || file.delete();
        File legacy = new File(context.getFilesDir(), LEGACY_FONT_FILE_NAME);
        if (legacy.exists()) {
            deleted &= legacy.delete();
        }
        preferences(context).edit().remove(KEY_EMOJI_FILENAME).apply();
        if (!hasAnyFont(context)) {
            writeEnabled(context, false);
        }
        return deleted;
    }

    public static boolean clearTextFont(Context context) {
        File file = getTextFontFile(context);
        boolean deleted = !file.exists() || file.delete();
        preferences(context).edit().remove(KEY_TEXT_FILENAME).apply();
        if (!hasAnyFont(context)) {
            writeEnabled(context, false);
        }
        return deleted;
    }

    public static boolean clearAll(Context context) {
        boolean ok = clearEmojiFont(context);
        ok &= clearTextFont(context);
        preferences(context).edit()
                .remove(KEY_ENABLED)
                .remove(KEY_MODE)
                .remove(KEY_EMOJI_FILENAME)
                .remove(KEY_TEXT_FILENAME)
                .apply();
        return ok;
    }
}
