package dev.jason.gboardpatches.patches.gboard.features.font

import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.features.featureflags.applyFeatureMarker
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

internal val gboardCustomFontFeatureMarkerPatch = resourcePatch(
    description = "標記 Custom Font & Modern Emoji feature 已被打入 target APK",
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize {
        applyFeatureMarker(CUSTOM_FONT_FEATURE_MARKER)
    }
}

internal const val CUSTOM_FONT_FEATURE_MARKER =
    "dev.jason.gboardpatches.feature.custom_font"
