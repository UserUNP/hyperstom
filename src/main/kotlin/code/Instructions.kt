package userunp.hyperstom.code

import kotlinx.serialization.Serializable
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.minestom.server.instance.Instance
import userunp.hyperstom.code.impl.*

val EMPTY_ARGS: Arguments = mutableMapOf()

data class InstContext(
    val controller: ExecController,
    val inst: Instruction,
    val instance: Instance,
    val target: ForwardingAudience,
)

interface InstFunction {
    val params: Array<out Parameter<out CodeValBox>>
    operator fun invoke(ctx: InstContext)
}

typealias InstList = MutableList<Instruction>
typealias InstLabelMap = MutableMap<CodeLabel, InstList>
typealias Arguments = MutableMap<String, CodeVal<*>>

@Serializable data class Instruction(
    val props: InstProperties,
    val args: Arguments,
    val target: EventTarget<*> = TARGET_NONE,
) {
    init {
        for (p in props.exec.params) {
            val codeVal = args[p.name]
            if (codeVal == null) {
                if (!p.optional) throw RuntimeException("Missing required argument! ${p.name}")
                args[p.name] = NULL_VALUE
            } else if (!p.optional && !p.dynamicType && !codeVal.type.runtime && p.type != null && codeVal.type != p.type) {
                throw RuntimeException("Expected ${p.type.name} but got ${codeVal.type.name} instead! ${props.name}")
            }
        }
    }
    operator fun invoke(
        controller: ExecController,
        instance: Instance,
        target: Set<Audience>,
    ) = props.exec(InstContext(controller, this, instance) { target })
}

enum class InstProperties(
    val exec: InstFunction,
    val targetClass: TargetClass = TargetClass.NONE,
) {
    // debugging
    DEBUG_FRAME(DebugFrame),
    // control
    CALL_FUNCTION(CallFunction),
    // var
    ASSIGN_VAR(AssignVar),
    // player/npc
    SEND_MESSAGE(SendMessage, TargetClass.PLAYER),
    ;
}

//TODO: plural parameters would be utilizing VAL_TYPE_LIST
data class Parameter<T : CodeValBox>(
    val name: String,
    val type: CodeValType<T>? = null,
    val dynamicType: Boolean = type == null,
    val optional: Boolean = false,
) {
    init {
        if (dynamicType && type != null) {
            throw RuntimeException("Cannot have a dynamic parameter with its type initialized! $name")
        }
    }
}
