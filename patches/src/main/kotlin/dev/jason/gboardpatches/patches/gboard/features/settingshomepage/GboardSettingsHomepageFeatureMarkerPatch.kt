package dev.jason.gboardpatches.patches.gboard.features.settingshomepage

import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.features.featureflags.applyFeatureMarker
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

internal val gboardSettingsHomepageFeatureMarkerPatch = resourcePatch(
    description = "標記 settings homepage feature 已被打入 target APK，共用 settings UI 過濾"
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize {
        applyFeatureMarker(SETTINGS_HOMEPAGE_FEATURE_MARKER_NAME)
    }
}

private const val SETTINGS_HOMEPAGE_FEATURE_MARKER_NAME =
    "dev.jason.gboardpatches.feature.settings_homepage"
