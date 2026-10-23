package dev.jason.gboardpatches.patches.gboard.features.font

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import dev.jason.gboardpatches.patches.gboard.shared.isOpcode
import dev.jason.gboardpatches.patches.gboard.shared.returnInstructionIndices
import dev.jason.gboardpatches.patches.gboard.shared.runtimeabi.RuntimeCallEmitter
import dev.jason.gboardpatches.patches.gboard.shared.runtimeabi.RuntimeCallId
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD_UNIVERSAL

private val AUTO_CENTER_SCALE_TEXT_VIEW_CANDIDATES = listOf(
    "Lcom/google/android/libraries/inputmethod/widgets/AutoCenterScaleTextView;",
    "Lcom/google/android/libraries/inputmethod/widgets/text/AutoCenterScaleTextView;",
)

private const val SOFT_KEY_VIEW_CLASS = "Lcom/google/android/libraries/inputmethod/widgets/SoftKeyView;"
private const val SOFT_KEYBOARD_VIEW_CLASS = "Lcom/google/android/libraries/inputmethod/widgets/SoftKeyboardView;"
private const val EMOJI_VIEW_CLASS = "Lcom/google/android/libraries/inputmethod/emoji/view/EmojiView;"

internal val gboardCustomFontBytecodePatch = bytecodePatch {
    compatibleWith(COMPATIBILITY_GBOARD_UNIVERSAL)

    execute {
        patchAutoCenterScaleTextView()
        patchSoftKeyboardView()
        patchSoftKeyView()
        patchEmojiViewAndRenderer()
    }
}

private fun BytecodePatchContext.patchAutoCenterScaleTextView() {
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

private fun BytecodePatchContext.patchSoftKeyboardView() {
    val softKeyboardViewClass = mutableClassDefByOrNull(SOFT_KEYBOARD_VIEW_CLASS) ?: return
    val onAttachedMethod = softKeyboardViewClass.methods.firstOrNull { method ->
        method.name == "onAttachedToWindow" && method.parameterTypes.isEmpty()
    }
    if (onAttachedMethod != null) {
        val onAttachedCall = RuntimeCallEmitter.invoke(
            RuntimeCallId.CUSTOM_FONT_RUNTIME_ON_KEYBOARD_VIEW_ATTACHED,
            "p0"
        )
        onAttachedMethod.addInstructions(0, onAttachedCall)
    }
}

private fun BytecodePatchContext.patchSoftKeyView() {
    val softKeyViewClass = mutableClassDefByOrNull(SOFT_KEY_VIEW_CLASS) ?: return
    val qMethod = softKeyViewClass.methods.firstOrNull { method ->
        method.name == "q" && method.parameterTypes == listOf("I", "Ljava/lang/CharSequence;")
    } ?: return

    val instructions = qMethod.implementation?.instructions ?: return
    val setTextIndex = instructions.indexOfFirst { instruction ->
        (instruction as? ReferenceInstruction)?.reference?.toString()
            ?.contains("TextView;->setText(Ljava/lang/CharSequence;)V") == true
    }
    if (setTextIndex >= 0) {
        val setTextInst = instructions[setTextIndex] as? FiveRegisterInstruction
        val viewReg = setTextInst?.registerC ?: 0
        val applyKeyCall = RuntimeCallEmitter.invoke(
            RuntimeCallId.CUSTOM_FONT_RUNTIME_APPLY_TYPEFACE_TO_KEY,
            "v$viewReg, p2"
        )
        qMethod.addInstructions(setTextIndex + 1, applyKeyCall)
    }
}

private fun BytecodePatchContext.patchEmojiViewAndRenderer() {
    val emojiViewClass = mutableClassDefByOrNull(EMOJI_VIEW_CLASS) ?: return

    val hMethod = emojiViewClass.methods.firstOrNull { method ->
        method.name == "h" && method.parameterTypes.isEmpty() && method.returnType == "Landroid/graphics/Paint;"
    }
    if (hMethod != null) {
        val returns = hMethod.returnInstructionIndices().filter { index ->
            hMethod.implementation!!.instructions[index].isOpcode("RETURN_OBJECT")
        }
        for (returnIndex in returns.asReversed()) {
            val returnInst = hMethod.implementation!!.instructions[returnIndex] as? OneRegisterInstruction
            val retReg = returnInst?.registerA ?: 0
            val applyPaintCall = RuntimeCallEmitter.invoke(
                RuntimeCallId.CUSTOM_FONT_RUNTIME_APPLY_TYPEFACE_TO_PAINT,
                "v$retReg"
            )
            hMethod.addInstructions(returnIndex, applyPaintCall)
        }
    }

    for (initMethod in emojiViewClass.methods.filter {
        it.name == "<init>" && it.parameterTypes.isNotEmpty() && it.parameterTypes[0] == "Landroid/content/Context;"
    }) {
        val initCall = RuntimeCallEmitter.invoke(
            RuntimeCallId.CUSTOM_FONT_RUNTIME_APPLY_CUSTOM_FONT,
            "p1"
        )
        initMethod.addInstructions(0, initCall)
    }

    val rendererTypeCandidates = listOfNotNull(
        emojiViewClass.fields.firstOrNull { it.name == "g" }?.type,
        "Lnts;"
    ).distinct()

    for (rendererType in rendererTypeCandidates) {
        val rendererClass = mutableClassDefByOrNull(rendererType) ?: continue
        for (initMethod in rendererClass.methods.filter { it.name == "<init>" }) {
            val instructions = initMethod.implementation?.instructions ?: continue
            val textPaintInitIndex = instructions.indexOfFirst { inst ->
                (inst as? ReferenceInstruction)?.reference?.toString() == "Landroid/text/TextPaint;-><init>()V"
            }
            if (textPaintInitIndex >= 0) {
                val inst = instructions[textPaintInitIndex] as? FiveRegisterInstruction
                val paintReg = inst?.registerC ?: 0
                val applyPaintCall = RuntimeCallEmitter.invoke(
                    RuntimeCallId.CUSTOM_FONT_RUNTIME_APPLY_TYPEFACE_TO_PAINT,
                    "v$paintReg"
                )
                initMethod.addInstructions(textPaintInitIndex + 1, applyPaintCall)
            }
        }
    }
}
