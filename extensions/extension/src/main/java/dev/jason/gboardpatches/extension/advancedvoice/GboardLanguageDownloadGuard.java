package dev.jason.gboardpatches.extension.advancedvoice;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Lets Gboard request the download of a language pack only once per language and source in
 * each process.
 */
public final class GboardLanguageDownloadGuard {
    private static final ConcurrentHashMap<String, Boolean> REQUESTED =
            new ConcurrentHashMap<String, Boolean>();

    private GboardLanguageDownloadGuard() {
    }

    /** Returns true when an identical request was already let through; the caller drops it. */
    public static boolean shouldSkip(String languageTag, Object source) {
        try {
            return languageTag != null
                    && REQUESTED.putIfAbsent(languageTag + '|' + source, Boolean.TRUE) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    static void resetForTest() {
        REQUESTED.clear();
    }
}
