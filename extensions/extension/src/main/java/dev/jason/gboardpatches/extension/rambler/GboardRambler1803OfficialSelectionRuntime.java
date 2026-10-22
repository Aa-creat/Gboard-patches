package dev.jason.gboardpatches.extension.rambler;

import android.content.Context;

import java.lang.reflect.Method;

/** Keeps Agentic capability exposure aligned with Gboard's official selector. */
public final class GboardRambler1803OfficialSelectionRuntime {
    private static final ThreadLocal<Integer> VOICE_SETTINGS_SCOPE_DEPTH =
            new ThreadLocal<Integer>();
    private static final ThreadLocal<Integer> DEFAULT_SELECTION_SUPPRESSION_DEPTH =
            new ThreadLocal<Integer>();

    private static volatile Boolean officialRamblerSelected;

    private GboardRambler1803OfficialSelectionRuntime() {
    }

    public static boolean shouldEnableAgenticDictation() {
        if (isDefaultSelectionSuppressed()) {
            return false;
        }
        if (isVoiceSettingsScopeActive()) {
            return true;
        }
        Boolean selected = officialRamblerSelected;
        if (selected == null) {
            selected = readOfficialSelection();
        }
        return Boolean.TRUE.equals(selected);
    }

    public static void enterVoiceSettingsScope() {
        VOICE_SETTINGS_SCOPE_DEPTH.set(Integer.valueOf(depth(VOICE_SETTINGS_SCOPE_DEPTH) + 1));
    }

    public static void exitVoiceSettingsScope() {
        decrement(VOICE_SETTINGS_SCOPE_DEPTH);
    }

    public static void updateOfficialSelection(boolean selected) {
        officialRamblerSelected = Boolean.valueOf(selected);
    }

    public static void enterDefaultSelectionSuppression() {
        DEFAULT_SELECTION_SUPPRESSION_DEPTH.set(Integer.valueOf(
                depth(DEFAULT_SELECTION_SUPPRESSION_DEPTH) + 1));
    }

    public static void exitDefaultSelectionSuppression() {
        decrement(DEFAULT_SELECTION_SUPPRESSION_DEPTH);
    }

    private static boolean isVoiceSettingsScopeActive() {
        return depth(VOICE_SETTINGS_SCOPE_DEPTH) > 0;
    }

    private static boolean isDefaultSelectionSuppressed() {
        return depth(DEFAULT_SELECTION_SUPPRESSION_DEPTH) > 0;
    }

    private static int depth(ThreadLocal<Integer> scope) {
        Integer value = scope.get();
        return value == null ? 0 : value.intValue();
    }

    private static void decrement(ThreadLocal<Integer> scope) {
        int next = depth(scope) - 1;
        if (next <= 0) {
            scope.remove();
        } else {
            scope.set(Integer.valueOf(next));
        }
    }

    private static Boolean readOfficialSelection() {
        try {
            Object application = Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null);
            if (!(application instanceof Context)) {
                return null;
            }
            Context context = (Context) application;
            ClassLoader loader = context.getClassLoader();

            // 1. In 18.3.1+: check aaeo.a(Context)
            try {
                Class<?> support = Class.forName("aaeo", false, loader);
                Method selection = support.getDeclaredMethod("a", Context.class);
                selection.setAccessible(true);
                Object value = selection.invoke(null, context);
                if (value instanceof Boolean) {
                    officialRamblerSelected = (Boolean) value;
                    return officialRamblerSelected;
                }
            } catch (Throwable ignored) {
            }

            // 2. In 18.0.3: check mqk.a(Context)
            try {
                Class<?> support = Class.forName("mqk", false, loader);
                Method selection = support.getDeclaredMethod("a", Context.class);
                selection.setAccessible(true);
                Object value = selection.invoke(null, context);
                if (value instanceof Boolean) {
                    officialRamblerSelected = (Boolean) value;
                    return officialRamblerSelected;
                }
            } catch (Throwable ignored) {
            }

            // 3. Fallback: check SharedPreferences for "enable_jetson"
            try {
                android.content.SharedPreferences prefs =
                        android.preference.PreferenceManager.getDefaultSharedPreferences(context);
                if (prefs != null && prefs.contains("enable_jetson")) {
                    boolean val = prefs.getBoolean("enable_jetson", true);
                    officialRamblerSelected = Boolean.valueOf(val);
                    return officialRamblerSelected;
                }
            } catch (Throwable ignored) {
            }
        } catch (Throwable ignored) {
            // Application or the exact formal selector may not be ready yet.
        }
        return null;
    }

    static void resetForTests() {
        VOICE_SETTINGS_SCOPE_DEPTH.remove();
        DEFAULT_SELECTION_SUPPRESSION_DEPTH.remove();
        officialRamblerSelected = null;
    }
}
