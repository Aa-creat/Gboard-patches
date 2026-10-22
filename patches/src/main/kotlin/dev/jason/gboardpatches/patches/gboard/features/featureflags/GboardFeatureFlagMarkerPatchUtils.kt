package dev.jason.gboardpatches.patches.gboard.features.featureflags

import app.morphe.patcher.patch.ResourcePatchContext
import dev.jason.gboardpatches.patches.gboard.shared.childElements
import dev.jason.gboardpatches.patches.gboard.shared.ensureManifestMetaData

context(context: ResourcePatchContext)
internal fun applyFeatureMarker(markerName: String) = with(context) {
    document("AndroidManifest.xml").use { document ->
        val manifest = document.documentElement
        val application = manifest.childElements("application").firstOrNull()
            ?: error("Could not find application element in AndroidManifest.xml")

        ensureManifestMetaData(document, application, markerName, "true")
    }
}
