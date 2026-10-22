package dev.jason.gboardpatches.patches.gboard.features.developeroptions

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.features.featureflags.applyFeatureMarker
import dev.jason.gboardpatches.patches.gboard.shared.childElements
import dev.jason.gboardpatches.patches.gboard.shared.manifestAndroidAttribute
import dev.jason.gboardpatches.patches.gboard.shared.setManifestAndroidAttribute
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD
import org.w3c.dom.Element

internal val gboardDeveloperOptionsFeatureMarkerPatch = resourcePatch(
    description = "標記 Developer options feature 已被打入 target APK"
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize {
        applyFeatureMarker(DEVELOPER_OPTIONS_FEATURE_MARKER_NAME)
        ensurePackageQueries()
    }
}

context(context: ResourcePatchContext)
private fun ensurePackageQueries() = with(context) {
    document("AndroidManifest.xml").use { document ->
        val manifest = document.documentElement
        val queries = manifest.childElements("queries").firstOrNull()
            ?: document.createElement("queries").also { createdQueries ->
                val application = manifest.childElements("application").firstOrNull()
                if (application == null) {
                    manifest.appendChild(createdQueries)
                } else {
                    manifest.insertBefore(createdQueries, application)
                }
            }

        SUPPORTED_GBOARD_PACKAGES.forEach { packageName ->
            val packageQuery = queries.childElements("package").firstOrNull {
                it.manifestAndroidAttribute("name") == packageName
            } ?: document.createElement("package").also { createdPackage ->
                queries.appendChild(createdPackage)
            }
            packageQuery.setManifestAndroidAttribute("name", packageName)
        }
    }
}

private const val DEVELOPER_OPTIONS_FEATURE_MARKER_NAME =
    "dev.jason.gboardpatches.feature.developer_options"
private val SUPPORTED_GBOARD_PACKAGES = listOf(
    "com.google.android.inputmethod.latin",
    "com.google.android.inputmethod.latin.jason.dev",
    "dev.jason.com.google.android.inputmethod.latin"
)
