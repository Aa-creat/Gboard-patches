package dev.jason.gboardpatches.patches.gboard.features.latinglobe

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OffsetInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ThreeRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import dev.jason.gboardpatches.patches.gboard.shared.gboardPatchesExtensionCarrierPatch
import dev.jason.gboardpatches.patches.gboard.shared.VerifiedTransformationPlan
import dev.jason.gboardpatches.patches.gboard.shared.VerifiedTransformationState
import dev.jason.gboardpatches.patches.gboard.shared.applyVerified
import dev.jason.gboardpatches.patches.gboard.shared.isInvoke
import dev.jason.gboardpatches.patches.gboard.shared.isFieldReference
import dev.jason.gboardpatches.patches.gboard.shared.isMethodReference
import dev.jason.gboardpatches.patches.gboard.shared.isOpcode
import dev.jason.gboardpatches.patches.gboard.shared.isReference
import dev.jason.gboardpatches.patches.gboard.shared.isRegisterOperation
import dev.jason.gboardpatches.patches.gboard.shared.mutableClass
import dev.jason.gboardpatches.patches.gboard.shared.runtimeabi.RuntimeAbiCatalog
import dev.jason.gboardpatches.patches.gboard.shared.runtimeabi.RuntimeCallEmitter
import dev.jason.gboardpatches.patches.gboard.shared.runtimeabi.RuntimeCallId
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

private const val LATIN_IME_CLASS =
    "Lcom/google/android/apps/inputmethod/libs/latin5/LatinIme;"
private const val LATIN_RUNTIME_PARAMS_TYPE = "Lyaf;"
private const val LATIN_RUNTIME_PARAMS_TYPE_1854 = "Lawxf;"
private val LATIN_GLOBE_RUNTIME_CALL =
    RuntimeCallId.LATIN_GLOBE_KEY_IGNORE_INTERVAL_RUNTIME_APPLY_OVERRIDE
private val LATIN_GLOBE_RUNTIME_DESCRIPTOR = RuntimeAbiCatalog.abi(LATIN_GLOBE_RUNTIME_CALL).reference
private const val TARGET_METHOD_NAME = "O"
private const val TARGET_METHOD_NAME_1854 = "N"
private const val TARGET_DESCRIPTOR =
    "$LATIN_IME_CLASS->O()$LATIN_RUNTIME_PARAMS_TYPE"
private const val STOCK_REGISTER_COUNT = 11
private const val STOCK_RETURN_REGISTER = 0
private const val TARGET_INTERVAL_FIELD = "Lyaf;->h:F"
private const val TARGET_INTERVAL_FIELD_1854 = "Lawxf;->h:F"
private val TARGET_ACCESS_FLAGS =
    AccessFlags.PROTECTED.value or AccessFlags.STATIC.value or AccessFlags.FINAL.value

internal val gboardLatinGlobeKeyIgnoreIntervalBytecodePatch = bytecodePatch(
    description = "允許覆寫英文地球鍵忽略時間。"
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(gboardPatchesExtensionCarrierPatch)

    execute {
        val target = selectLatinGlobeFactory(mutableClass(LATIN_IME_CLASS).methods.toList())
        target.applyLatinGlobeKeyIgnoreIntervalOverride()
    }
}

internal fun selectLatinGlobeFactory(methods: List<MutableMethod>): MutableMethod {
    val matches = methods.filter { method ->
        method.definingClass == LATIN_IME_CLASS &&
            (method.name == TARGET_METHOD_NAME || method.name == TARGET_METHOD_NAME_1854) &&
            (method.returnType == LATIN_RUNTIME_PARAMS_TYPE || method.returnType == LATIN_RUNTIME_PARAMS_TYPE_1854) &&
            method.parameterTypes.isEmpty() &&
            method.accessFlags == TARGET_ACCESS_FLAGS
    }
    check(matches.size == 1) {
        "Expected exactly one protected static final Latin Globe target in $LATIN_IME_CLASS"
    }
    return matches.single()
}

internal fun MutableMethod.applyLatinGlobeKeyIgnoreIntervalOverride(): MutableMethod {
    requireExactTarget()
    return applyVerified(
        VerifiedTransformationPlan(
            targetName = "$definingClass->$name()$returnType",
            classify = MutableMethod::classifyLatinGlobeOverride,
            mutate = { method ->
                val instructions = method.implementation?.instructions
                    ?: error("No instructions available in ${method.definingClass}->${method.name}()${method.returnType}")
                method.addInstructions(
                    method.singleReturnIndex(instructions),
                    buildLatinGlobeDelegate(STOCK_RETURN_REGISTER, method.returnType),
                )
                method
            },
        ),
    )
}

private fun MutableMethod.classifyLatinGlobeOverride(): VerifiedTransformationState {
    val instructions = implementation?.instructions
        ?: error("No instructions available in $definingClass->$name()$returnType")
    return when (instructions.count { it.isMethodReference(LATIN_GLOBE_RUNTIME_DESCRIPTOR) }) {
        0 -> {
            validateStockBody(instructions)
            VerifiedTransformationState.STOCK
        }
        1 -> {
            validateCompletedPatch()
            VerifiedTransformationState.PATCHED
        }
        else -> VerifiedTransformationState.MALFORMED
    }
}

private fun MutableMethod.requireExactTarget() {
    check(
        definingClass == LATIN_IME_CLASS &&
            (name == TARGET_METHOD_NAME || name == TARGET_METHOD_NAME_1854) &&
            (returnType == LATIN_RUNTIME_PARAMS_TYPE || returnType == LATIN_RUNTIME_PARAMS_TYPE_1854) &&
            parameterTypes.isEmpty() &&
            accessFlags == TARGET_ACCESS_FLAGS,
    ) {
        "Refusing non-target Latin Globe method " +
            definingClass + "->" + name + "(" + parameterTypes.joinToString("") + ")" + returnType
    }
}

private fun MutableMethod.validateStockBody(instructions: List<Instruction>) {
    val implementation = implementation ?: error("No instructions available in $definingClass->$name()$returnType")
    check(implementation.registerCount == STOCK_REGISTER_COUNT) {
        "Unexpected register count in $definingClass->$name()$returnType: " + implementation.registerCount
    }
    check(implementation.tryBlocks.isEmpty()) {
        "Unexpected try blocks in exact $definingClass->$name()$returnType"
    }
    val returnIndex = singleReturnIndex(instructions)
    check(returnIndex == instructions.lastIndex) {
        "$definingClass->$name()$returnType must end at its single RETURN_OBJECT"
    }
    check((instructions[returnIndex] as OneRegisterInstruction).registerA == STOCK_RETURN_REGISTER) {
        "$definingClass->$name()$returnType must return exact v$STOCK_RETURN_REGISTER"
    }
    check(instructions.none { it.isMethodReference(LATIN_GLOBE_RUNTIME_DESCRIPTOR) }) {
        "$definingClass->$name()$returnType contains an orphan Latin Globe delegate"
    }
    validateTargetFieldWrite(instructions)
}

private fun MutableMethod.validateCompletedPatch() {
    requireExactTarget()
    val implementation = implementation ?: error("No instructions available in $definingClass->$name()$returnType")
    check(implementation.registerCount == STOCK_REGISTER_COUNT)
    check(implementation.tryBlocks.isEmpty())
    val instructions = implementation.instructions
    val returnIndex = singleReturnIndex(instructions)
    check(returnIndex == instructions.lastIndex)
    check((instructions[returnIndex] as OneRegisterInstruction).registerA == STOCK_RETURN_REGISTER)
    check(
        returnIndex >= 3 &&
            instructions[returnIndex - 3].isExactDelegateInvoke() &&
            instructions[returnIndex - 2].isExactOneRegister(
                "MOVE_RESULT_OBJECT",
                STOCK_RETURN_REGISTER,
            ) &&
            instructions[returnIndex - 1].isExactCast(
                STOCK_RETURN_REGISTER,
                returnType,
            ),
    ) {
        "$definingClass->$name()$returnType final Latin Globe delegate is missing or malformed"
    }
    check(instructions.count {
        it.isMethodReference(LATIN_GLOBE_RUNTIME_DESCRIPTOR)
    } == 1) {
        "$definingClass->$name()$returnType must contain exactly one Latin Globe delegate"
    }
    val stockInstructions = instructions.filterIndexed { index, _ ->
        index !in (returnIndex - 3)..(returnIndex - 1)
    }
    validateTargetFieldWrite(stockInstructions)
}

private fun MutableMethod.singleReturnIndex(instructions: List<Instruction>): Int {
    val returns = instructions.indices.filter { index ->
        instructions[index].isOpcode("RETURN_OBJECT")
    }
    check(returns.size == 1) {
        "$definingClass->$name()$returnType must contain exactly one RETURN_OBJECT"
    }
    return returns.single()
}

private fun MutableMethod.validateTargetFieldWrite(instructions: List<Instruction>) {
    val expectedField = if (returnType == LATIN_RUNTIME_PARAMS_TYPE_1854) {
        TARGET_INTERVAL_FIELD_1854
    } else {
        TARGET_INTERVAL_FIELD
    }
    check(instructions.count { it.isFieldReference(expectedField) } == 1) {
        "$definingClass->$name()$returnType must retain exactly one globe interval field write ($expectedField)"
    }
}

private fun Instruction.isExactDelegateInvoke(): Boolean =
    isInvoke(
        "INVOKE_STATIC",
        LATIN_GLOBE_RUNTIME_DESCRIPTOR,
        STOCK_RETURN_REGISTER,
    )

private fun Instruction.isExactOneRegister(opcode: String, register: Int): Boolean =
    isRegisterOperation(opcode, register)

private fun Instruction.isExactCast(register: Int, type: String): Boolean =
    isExactOneRegister("CHECK_CAST", register) &&
        isReference(type)

private fun buildLatinGlobeDelegate(register: Int, castType: String = LATIN_RUNTIME_PARAMS_TYPE): String = """
    ${RuntimeCallEmitter.invoke(LATIN_GLOBE_RUNTIME_CALL, "v$register")}

    move-result-object v$register

    check-cast v$register, $castType
""".trimIndent()
