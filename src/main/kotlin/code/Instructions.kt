package userunp.hyperstom.code

import kotlinx.serialization.Serializable
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.minestom.server.instance.Instance
import userunp.hyperstom.Identifiable
import userunp.hyperstom.code.impl.*
import kotlin.reflect.cast

val EMPTY_ARGS = Arguments()
private val NULL_VALUE_SET = mutableListOf(NULL_VALUE)

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
typealias InstLabelMap = MutableMap<InstListLabel, InstList>
@Serializable data class Instruction(
    val props: InstProperties,
    val args: Arguments,
    val target: EventTarget<*> = TARGET_NONE,
) {
    init {
        for (p in props.exec.params) {
            val valuesSet = args.map[p.name]
                ?: if (p.optional) NULL_VALUE_SET else throw RuntimeException("Missing required argument! ${p.name}")
            for (value in valuesSet) if (value.type != p.type) {
                throw RuntimeException("Expected ${p.type.name} but got ${value.type} instead!")
            }
            args.map[p.name] = valuesSet
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
    PRINT_INST_LIST(PrintInstList),
    // control
    CALL_FUNCTION(CallFunction),
    // player/npc
    SEND_MESSAGE(SendMessage, TargetClass.PLAYER),
    ;
}

@Serializable data class Arguments(val map: MutableMap<String, MutableList<out CodeValue<*>>>) {
    constructor(vararg pairs: Pair<String, MutableList<CodeValue<*>>>) : this(mutableMapOf(*pairs))
    fun <T : CodeValBox> single(p: Parameter<T>): T {
        val arg = (map[p.name] ?: throw RuntimeException("No such parameter! $p"))[0]
        if (p.type != arg.type) throw RuntimeException("Expected ${p.type.name}, got ${arg.type.name} instead! ${p.name}")
        return p.type.typeClass.cast(arg.value)
    }
}

data class Parameter<T : CodeValBox>(
    override val name: String,
    val type: CodeValueType<T>,
    val optional: Boolean = false,
    val plural: Boolean = false
) : Identifiable
