package dev.jason.gboardpatches.patches.gboard.features.clipboard

import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.features.featureflags.applyFeatureMarker
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

internal val gboardClipboardFeatureMarkerPatch = resourcePatch(
    description = "標記 clipboard feature 已被打入 target APK，共用 settings UI 過濾"
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize {
        applyFeatureMarker(CLIPBOARD_FEATURE_MARKER_NAME)
    }
}

private const val CLIPBOARD_FEATURE_MARKER_NAME =
    "dev.jason.gboardpatches.feature.clipboard_enhancements"
