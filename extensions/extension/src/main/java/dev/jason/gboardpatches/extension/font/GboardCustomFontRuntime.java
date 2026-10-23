package dev.jason.gboardpatches.extension.font;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class GboardCustomFontRuntime {
    private static final String TAG = "GboardCustomFont";
    public static final String ASSET_FONT_PATH = "gboard/custom-font.ttf";

    private static final String[] SYSTEM_FAMILY_NAMES = {
            "sans-serif",
            "sans-serif-thin",
            "sans-serif-light",
            "sans-serif-medium",
            "sans-serif-black",
            "sans-serif-condensed",
            "sans-serif-condensed-light",
            "sans-serif-condensed-medium",
            "roboto",
    };

    private static final String[] EMOJI_FAMILY_NAMES = {
            "emoji",
            "noto-color-emoji",
            "notocoloremoji",
            "emoji-compat",
            "color-emoji",
            "android/emoji"
    };

    private static final Map<View, Typeface> ORIGINAL_TYPEFACES =
            Collections.synchronizedMap(new WeakHashMap<View, Typeface>());

    private static volatile Typeface sCustomTypeface = null;
    private static volatile Typeface sEmojiTypeface = null;
    private static volatile Typeface sCompositeEmojiTypeface = null;
    private static volatile boolean sApplied = false;
    public static volatile boolean sEmojiOnlyMode = true;

    private GboardCustomFontRuntime() {
    }

    /**
     * Re-applies font configuration, resetting the applied latch.
     */
    public static void reload(Context context) {
        synchronized (GboardCustomFontRuntime.class) {
            sApplied = false;
            applyCustomFont(context);
        }
    }

    /**
     * Cold-start / lifecycle injection point: called before or during Gboard view creation.
     */
    public static void applyCustomFont(Context context) {
        if (context == null || sApplied) {
            return;
        }

        synchronized (GboardCustomFontRuntime.class) {
            if (sApplied) {
                return;
            }

            try {
                Context appContext = context.getApplicationContext() != null
                        ? context.getApplicationContext()
                        : context;

                // Sync bundled asset font to app storage if not already present
                syncBundledAssetFontIfPresent(appContext);

                if (!GboardCustomFontSettings.isCustomFontActive(appContext)) {
                    Log.d(TAG, "Custom font is not active in settings");
                    sCustomTypeface = null;
                    sEmojiTypeface = null;
                    sCompositeEmojiTypeface = null;
                    return;
                }

                String mode = GboardCustomFontSettings.readMode(appContext);
                boolean isEmojiOnly = GboardCustomFontSettings.MODE_EMOJI_ONLY.equals(mode);
                sEmojiOnlyMode = isEmojiOnly;

                File emojiFile = GboardCustomFontSettings.getEmojiFontFile(appContext);
                File textFile = GboardCustomFontSettings.getTextFontFile(appContext);

                Typeface regularTypeface;
                Typeface boldTypeface;
                Typeface emojiTypeface = emojiFile.isFile() ? loadTypeface(emojiFile) : null;
                sEmojiTypeface = emojiTypeface;

                if (isEmojiOnly && emojiFile.isFile()) {
                    // Smart Emoji Mode: Build composite font preserving system CJK/Latin typography
                    sCompositeEmojiTypeface = createCompositeEmojiTypeface(emojiFile);
                    regularTypeface = sCompositeEmojiTypeface != null ? sCompositeEmojiTypeface : emojiTypeface;
                    boldTypeface = regularTypeface != null ? Typeface.create(regularTypeface, Typeface.BOLD) : null;
                    Log.i(TAG, "Loaded composite Smart Emoji typeface");
                } else {
                    // Global Mode: Use custom text font (or emoji font if no text font provided)
                    File target = textFile.isFile() ? textFile : emojiFile;
                    regularTypeface = loadTypeface(target);
                    boldTypeface = regularTypeface != null ? Typeface.create(regularTypeface, Typeface.BOLD) : null;
                    Log.i(TAG, "Loaded global custom typeface: " + (target != null ? target.getName() : "null"));
                }

                if (regularTypeface != null) {
                    sCustomTypeface = regularTypeface;
                    replaceSystemTypefaces(regularTypeface, boldTypeface, emojiTypeface);
                    Log.i(TAG, "Successfully injected custom font and emoji into Typeface system maps");
                }
                sApplied = true;
            } catch (Throwable throwable) {
                Log.e(TAG, "Failed to apply custom font", throwable);
            }
        }
    }

    private static void syncBundledAssetFontIfPresent(Context context) {
        try {
            File emojiFile = GboardCustomFontSettings.getEmojiFontFile(context);
            if (!emojiFile.exists()) {
                String[] assets = context.getAssets().list("gboard");
                if (assets != null) {
                    for (String asset : assets) {
                        if ("custom-font.ttf".equals(asset)) {
                            try (InputStream in = context.getAssets().open(ASSET_FONT_PATH);
                                 OutputStream out = new FileOutputStream(emojiFile)) {
                                byte[] buffer = new byte[64 * 1024];
                                int read;
                                while ((read = in.read(buffer)) != -1) {
                                    out.write(buffer, 0, read);
                                }
                                out.flush();
                            }
                            GboardCustomFontSettings.writeEnabled(context, true);
                            Log.i(TAG, "Pre-bundled asset font copied to " + emojiFile.getName());
                            break;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public static Typeface loadTypeface(File file) {
        if (file == null || !file.isFile()) {
            return null;
        }
        try {
            return new Typeface.Builder(file).build();
        } catch (Throwable t) {
            try {
                return Typeface.createFromFile(file);
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private static Typeface createCompositeEmojiTypeface(File emojiFontFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                android.graphics.fonts.Font systemBaseFont = null;
                Set<android.graphics.fonts.Font> available =
                        android.graphics.fonts.SystemFonts.getAvailableFonts();
                for (android.graphics.fonts.Font font : available) {
                    File file = font.getFile();
                    if (file != null && "Roboto-Regular.ttf".equalsIgnoreCase(file.getName())) {
                        systemBaseFont = font;
                        break;
                    }
                }
                if (systemBaseFont == null) {
                    for (android.graphics.fonts.Font font : available) {
                        File file = font.getFile();
                        if (file != null && !file.getName().toLowerCase(Locale.US).contains("emoji")) {
                            systemBaseFont = font;
                            break;
                        }
                    }
                }

                if (systemBaseFont != null) {
                    android.graphics.fonts.FontFamily sysFamily =
                            new android.graphics.fonts.FontFamily.Builder(systemBaseFont).build();
                    android.graphics.fonts.Font emojiFont =
                            new android.graphics.fonts.Font.Builder(emojiFontFile).build();
                    android.graphics.fonts.FontFamily emojiFamily =
                            new android.graphics.fonts.FontFamily.Builder(emojiFont).build();

                    return new Typeface.CustomFallbackBuilder(sysFamily)
                            .addCustomFallback(emojiFamily)
                            .setSystemFallback("sans-serif")
                            .build();
                }
            } catch (Throwable t) {
                Log.w(TAG, "CustomFallbackBuilder failed, falling back to direct Typeface: " + t.getMessage());
            }
        }
        return loadTypeface(emojiFontFile);
    }

    public static void replaceSystemTypefaces(Typeface regular, Typeface bold, Typeface emoji) {
        if (regular == null) {
            return;
        }
        replaceSystemFontMap(regular, emoji);
        replaceStaticField("DEFAULT", regular);
        replaceStaticField("DEFAULT_BOLD", bold != null ? bold : regular);
        replaceStaticField("SANS_SERIF", regular);
    }

    @SuppressWarnings("unchecked")
    private static void replaceSystemFontMap(Typeface regular, Typeface emoji) {
        try {
            Field field = Typeface.class.getDeclaredField("sSystemFontMap");
            field.setAccessible(true);
            Map<String, Typeface> map = (Map<String, Typeface>) field.get(null);
            if (map == null) {
                Log.w(TAG, "sSystemFontMap is null");
                return;
            }

            Map<String, Typeface> replacements = new HashMap<>();
            for (String name : SYSTEM_FAMILY_NAMES) {
                replacements.put(name, regular);
            }
            if (emoji != null) {
                for (String emojiName : EMOJI_FAMILY_NAMES) {
                    replacements.put(emojiName, emoji);
                }
            }

            try {
                map.putAll(replacements);
            } catch (UnsupportedOperationException ex) {
                Map<String, Typeface> copy = new HashMap<>(map);
                copy.putAll(replacements);
                field.set(null, copy);
            }
            Log.d(TAG, "Injected custom typefaces into sSystemFontMap");
        } catch (Throwable t) {
            Log.w(TAG, "Could not replace sSystemFontMap: " + t.getMessage());
        }
    }

    private static void replaceStaticField(String fieldName, Typeface value) {
        try {
            Field field = Typeface.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Throwable t) {
            Log.w(TAG, "Could not replace Typeface." + fieldName + ": " + t.getMessage());
        }
    }

    /**
     * Resolves the appropriate typeface based on content and user settings.
     * Kept for AutoCenterScaleTextView and manual view hooks.
     */
    public static Typeface resolveTypeface(View view, Typeface incoming) {
        Typeface custom = getCustomTypeface(view);
        if (custom == null || view == null) {
            return incoming;
        }

        if (incoming != null && incoming != custom) {
            ORIGINAL_TYPEFACES.put(view, incoming);
        }
        Typeface original = ORIGINAL_TYPEFACES.get(view);
        if (original == null) {
            original = incoming;
        }

        if (sEmojiOnlyMode && view instanceof TextView) {
            CharSequence text = ((TextView) view).getText();
            if (text == null || text.length() == 0 || !containsEmoji(text)) {
                return original;
            }
        }

        return custom;
    }

    public static Typeface getCustomTypeface(View view) {
        if (sCustomTypeface != null) {
            return sCustomTypeface;
        }
        if (view != null) {
            applyCustomFont(view.getContext());
        }
        return sCustomTypeface;
    }

    public static Typeface getEmojiTypeface() {
        return sEmojiTypeface;
    }

    /**
     * Applies the custom emoji/text typeface to an arbitrary Paint/TextPaint.
     * Used by EmojiView.h() and background emoji bitmap rendering workers.
     */
    public static void applyTypefaceToPaint(Paint paint) {
        if (paint == null) {
            return;
        }
        Typeface tf = sEmojiTypeface != null ? sEmojiTypeface : sCustomTypeface;
        if (tf != null) {
            paint.setTypeface(tf);
        }
    }

    /**
     * Direct keycaps hook: invoked immediately after SoftKeyView.q sets key label text.
     * In Smart Emoji mode, only emojis use the custom emoji font while stock font is preserved
     * for Latin, Zhuyin, and symbols. In Global mode, all keys get the custom typeface.
     */
    public static void applyTypefaceToKey(View view, CharSequence text) {
        if (!(view instanceof TextView)) {
            return;
        }
        TextView tv = (TextView) view;
        Context context = tv.getContext();
        applyCustomFont(context);

        if (!GboardCustomFontSettings.isCustomFontActive(context)) {
            return;
        }

        if (!ORIGINAL_TYPEFACES.containsKey(tv)) {
            ORIGINAL_TYPEFACES.put(tv, tv.getTypeface());
        }

        if (sEmojiOnlyMode) {
            if (containsEmoji(text)) {
                Typeface tf = sEmojiTypeface != null ? sEmojiTypeface : sCustomTypeface;
                if (tf != null) {
                    tv.setTypeface(tf);
                }
            } else {
                Typeface original = ORIGINAL_TYPEFACES.get(tv);
                if (original != null) {
                    tv.setTypeface(original);
                }
            }
        } else {
            if (sCustomTypeface != null) {
                tv.setTypeface(sCustomTypeface);
            }
        }
    }

    /**
     * SoftKeyboardView lifecycle hook: guarantees runtime is loaded as soon as keyboard appears.
     */
    public static void onSoftKeyboardViewAttached(View view) {
        if (view != null) {
            applyCustomFont(view.getContext());
        }
    }

    public static void setCustomTypefaceForTesting(Typeface typeface) {
        sCustomTypeface = typeface;
        sEmojiTypeface = typeface;
        sApplied = (typeface != null);
    }

    public static void resetForTesting() {
        sCustomTypeface = null;
        sEmojiTypeface = null;
        sCompositeEmojiTypeface = null;
        sApplied = false;
        sEmojiOnlyMode = true;
        ORIGINAL_TYPEFACES.clear();
    }

    public static boolean containsEmoji(CharSequence sequence) {
        if (sequence == null) {
            return false;
        }
        int length = sequence.length();
        for (int i = 0; i < length; i++) {
            int codePoint = Character.codePointAt(sequence, i);
            if (isEmojiCodePoint(codePoint)) {
                return true;
            }
            if (Character.isSupplementaryCodePoint(codePoint)) {
                i++;
            }
        }
        return false;
    }

    public static boolean isEmojiCodePoint(int cp) {
        return (cp >= 0x1F000 && cp <= 0x1FAFF)
                || (cp >= 0x2300 && cp <= 0x23FF)
                || (cp >= 0x2600 && cp <= 0x27BF)
                || (cp >= 0x2B00 && cp <= 0x2BFF)
                || (cp >= 0xFE00 && cp <= 0xFE0F)
                || (cp >= 0xE0020 && cp <= 0xE007F)
                || (cp == 0x3030 || cp == 0x303D || cp == 0x3297 || cp == 0x3299);
    }
}
