package dev.jason.gboardpatches.extension.font;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class GboardCustomFontRuntime {
    private static final String TAG = "GboardCustomFont";
    public static final String ASSET_FONT_PATH = "gboard/custom-font.ttf";

    private static final Map<View, Typeface> ORIGINAL_TYPEFACES =
            Collections.synchronizedMap(new WeakHashMap<View, Typeface>());

    private static volatile Typeface sCustomTypeface = null;
    private static volatile boolean sLoadAttempted = false;
    public static volatile boolean sEmojiOnlyMode = true;

    private GboardCustomFontRuntime() {
    }

    /**
     * Resolves the appropriate typeface based on content and user settings.
     *
     * @param view The AutoCenterScaleTextView or View being rendered
     * @param incoming The incoming or current typeface
     * @return The custom typeface if content contains emoji (or in global mode), otherwise original
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
        if (sLoadAttempted || view == null) {
            return null;
        }
        synchronized (GboardCustomFontRuntime.class) {
            if (sLoadAttempted) {
                return sCustomTypeface;
            }
            sLoadAttempted = true;
            try {
                Context context = view.getContext();
                if (context != null) {
                    try {
                        String[] gboardAssets = context.getAssets().list("gboard");
                        if (gboardAssets != null) {
                            for (String item : gboardAssets) {
                                if ("font-mode-global.marker".equals(item)) {
                                    sEmojiOnlyMode = false;
                                    Log.i(TAG, "Global font mode enabled by marker.");
                                    break;
                                }
                            }
                        }
                    } catch (Throwable ignored) {
                    }

                    sCustomTypeface = Typeface.createFromAsset(context.getAssets(), ASSET_FONT_PATH);
                    Log.i(TAG, "Custom font loaded successfully from assets: " + ASSET_FONT_PATH);
                    return sCustomTypeface;
                }
            } catch (Throwable t) {
                Log.w(TAG, "No custom font in assets or failed to load: " + t.getMessage());
            }
        }
        return null;
    }

    public static void setCustomTypefaceForTesting(Typeface typeface) {
        sCustomTypeface = typeface;
        sLoadAttempted = (typeface != null);
    }

    public static void resetForTesting() {
        sCustomTypeface = null;
        sLoadAttempted = false;
        sEmojiOnlyMode = true;
        ORIGINAL_TYPEFACES.clear();
    }

    /**
     * Fast check if the character sequence contains any Emoji code points.
     */
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
        return (cp >= 0x1F300 && cp <= 0x1FAFF)   // Symbols, Pictographs, Supplemental Symbols
            || (cp >= 0x1F600 && cp <= 0x1F64F)   // Emoticons
            || (cp >= 0x1F680 && cp <= 0x1F6FF)   // Transport and Map
            || (cp >= 0x2600 && cp <= 0x27BF)     // Misc symbols & Dingbats
            || (cp >= 0xFE00 && cp <= 0xFE0F)     // Variation Selectors
            || (cp >= 0x1F900 && cp <= 0x1F9FF)   // Supplemental Symbols and Pictographs
            || (cp >= 0xE0020 && cp <= 0xE007F);  // Tags (Flags etc.)
    }
}
