package dev.jason.gboardpatches.patches.gboard.features.clipboard

import app.morphe.patcher.patch.BytecodePatchContext
import dev.jason.gboardpatches.patches.gboard.shared.runtimeabi.RuntimeAbiCatalog
import dev.jason.gboardpatches.patches.gboard.shared.runtimeabi.RuntimeCallId

internal val CLIPBOARD_RUNTIME_CLASS = RuntimeAbiCatalog.abi(
    RuntimeCallId.CLIPBOARD_RUNTIME_AFTER_ADAPTER_TRIM,
).owner
internal const val CLIPBOARD_LOADER_CALLABLE_CLASS = "Lfik;"
internal const val CLIPBOARD_PRUNE_CALLABLE_CLASS = "Lfjv;"
internal const val CLIPBOARD_ADAPTER_CLASS = "Lfjk;"
internal const val CLIPBOARD_KEYBOARD_CLASS =
    "Lcom/google/android/apps/inputmethod/libs/clipboard/ClipboardKeyboard;"
internal const val RECYCLER_VIEW_HOLDER_CLASS = "Lkr;"

internal data class ClipboardTargets(
    val adapterClass: String,
    val pruneClass: String,
    val loaderClass: String,
    val keyboardClass: String,
    val viewHolderClass: String,
    val columnCountMethod: String,
)

internal val CLIPBOARD_1803_TARGETS = ClipboardTargets(
    adapterClass = CLIPBOARD_ADAPTER_CLASS,
    pruneClass = CLIPBOARD_PRUNE_CALLABLE_CLASS,
    loaderClass = CLIPBOARD_LOADER_CALLABLE_CLASS,
    keyboardClass = CLIPBOARD_KEYBOARD_CLASS,
    viewHolderClass = RECYCLER_VIEW_HOLDER_CLASS,
    columnCountMethod = "l",
)

internal val CLIPBOARD_1854_TARGETS = ClipboardTargets(
    adapterClass = "Llcl;",
    pruneClass = "Lldo;",
    loaderClass = "Llcz;",
    keyboardClass = CLIPBOARD_KEYBOARD_CLASS,
    viewHolderClass = "Lra;",
    columnCountMethod = "b",
)

internal fun resolveClipboardTargets(context: BytecodePatchContext): ClipboardTargets =
    if (context.mutableClassDefByOrNull(CLIPBOARD_1854_TARGETS.adapterClass) != null) {
        CLIPBOARD_1854_TARGETS
    } else {
        CLIPBOARD_1803_TARGETS
    }
