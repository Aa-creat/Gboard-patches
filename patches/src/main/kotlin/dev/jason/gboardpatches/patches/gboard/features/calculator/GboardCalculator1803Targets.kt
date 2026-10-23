package dev.jason.gboardpatches.patches.gboard.features.calculator

import app.morphe.patcher.patch.BytecodePatchContext
import dev.jason.gboardpatches.patches.gboard.shared.GboardFieldTarget
import dev.jason.gboardpatches.patches.gboard.shared.GboardMethodTarget

internal object GboardCalculator1803Targets {
    const val OWNER_1803 = "Loup;"
    const val OWNER_1854 = "Laeut;"

    fun resolveOwner(context: BytecodePatchContext): String =
        if (context.mutableClassDefByOrNull(OWNER_1854) != null) OWNER_1854 else OWNER_1803

    fun inputView(context: BytecodePatchContext) = GboardFieldTarget(
        classType = resolveOwner(context),
        name = "j",
        type = "Lcom/google/android/libraries/inputmethod/inputview/InputView;",
    )

    fun onStartInputView(context: BytecodePatchContext) = GboardMethodTarget(
        classType = resolveOwner(context),
        name = "onStartInputView",
        parameterTypes = listOf("Landroid/view/inputmethod/EditorInfo;", "Z"),
        returnType = "V",
    )

    fun onUpdateSelection(context: BytecodePatchContext) = GboardMethodTarget(
        classType = resolveOwner(context),
        name = "onUpdateSelection",
        parameterTypes = listOf("I", "I", "I", "I", "I", "I"),
        returnType = "V",
    )

    fun onWindowHidden(context: BytecodePatchContext) = GboardMethodTarget(
        classType = resolveOwner(context),
        name = "onWindowHidden",
        parameterTypes = emptyList(),
        returnType = "V",
    )

    val inputView = GboardFieldTarget(
        classType = "Loup;",
        name = "j",
        type = "Lcom/google/android/libraries/inputmethod/inputview/InputView;",
    )
    val onStartInputView = GboardMethodTarget(
        classType = OWNER_1803,
        name = "onStartInputView",
        parameterTypes = listOf("Landroid/view/inputmethod/EditorInfo;", "Z"),
        returnType = "V",
    )
    val onUpdateSelection = GboardMethodTarget(
        classType = OWNER_1803,
        name = "onUpdateSelection",
        parameterTypes = listOf("I", "I", "I", "I", "I", "I"),
        returnType = "V",
    )
    val onWindowHidden = GboardMethodTarget(
        classType = OWNER_1803,
        name = "onWindowHidden",
        parameterTypes = emptyList(),
        returnType = "V",
    )
}

