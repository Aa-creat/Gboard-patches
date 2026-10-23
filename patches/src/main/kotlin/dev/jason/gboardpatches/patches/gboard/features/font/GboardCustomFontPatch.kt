package dev.jason.gboardpatches.patches.gboard.features.font

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import dev.jason.gboardpatches.patches.gboard.shared.runtimeabi.RuntimeCallEmitter
import dev.jason.gboardpatches.patches.gboard.shared.runtimeabi.RuntimeCallId
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD_UNIVERSAL

private val AUTO_CENTER_SCALE_TEXT_VIEW_CANDIDATES = listOf(
    "Lcom/google/android/libraries/inputmethod/widgets/AutoCenterScaleTextView;",
    "Lcom/google/android/libraries/inputmethod/widgets/text/AutoCenterScaleTextView;",
)

internal val gboardCustomFontBytecodePatch = bytecodePatch {
    compatibleWith(COMPATIBILITY_GBOARD_UNIVERSAL)

    execute {
        val targetClasses = AUTO_CENTER_SCALE_TEXT_VIEW_CANDIDATES.mapNotNull { className ->
            mutableClassDefByOrNull(className)
        }
        require(targetClasses.isNotEmpty()) {
            "Could not find any AutoCenterScaleTextView class in candidates: $AUTO_CENTER_SCALE_TEXT_VIEW_CANDIDATES"
        }

        for (owner in targetClasses) {
            val setTypefaceMethod = owner.methods.firstOrNull { method ->
                method.name == "setTypeface" && method.parameterTypes == listOf("Landroid/graphics/Typeface;")
            }
            val resolveTypefaceP0P1 = RuntimeCallEmitter.invoke(
                RuntimeCallId.CUSTOM_FONT_RUNTIME_RESOLVE_TYPEFACE,
                "p0, p1"
            )
            setTypefaceMethod?.addInstructions(
                0,
                """
                    $resolveTypefaceP0P1
                    move-result-object p1
                """,
            )

            val onDrawMethod = owner.methods.firstOrNull { method ->
                method.name == "onDraw" && method.parameterTypes == listOf("Landroid/graphics/Canvas;")
            }
            val resolveTypefaceP0V0 = RuntimeCallEmitter.invoke(
                RuntimeCallId.CUSTOM_FONT_RUNTIME_RESOLVE_TYPEFACE,
                "p0, v0"
            )
            onDrawMethod?.addInstructionsWithLabels(
                0,
                """
                    invoke-virtual {p0}, Landroid/widget/TextView;->getTypeface()Landroid/graphics/Typeface;
                    move-result-object v0
                    $resolveTypefaceP0V0
                    move-result-object v1
                    if-eq v0, v1, :font_skip
                    invoke-virtual {p0, v1}, ${owner.type}->setTypeface(Landroid/graphics/Typeface;)V
                    :font_skip
                    nop
                """,
            )
        }
    }
}
