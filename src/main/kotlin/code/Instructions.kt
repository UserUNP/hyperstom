package ma.userunp.hyperstom.code

import ma.userunp.hyperstom.Named
import net.kyori.adventure.audience.ForwardingAudience
import net.minestom.server.instance.Instance

class InstContext(
    executor: CodeExecutor,
    val instance: Instance,
    val target: ForwardingAudience,
) : CodeExecutor by executor

interface InstType : Named {
    val targetClass: TargetClass
    val params: Array<out ParamNode<*>>
    fun exec(ctx: InstContext)
}

typealias InstList = MutableList<CodeInst>
typealias InstLabelMap = LinkedHashMap<CodeLabel<*>, InstList>
typealias RawArgs = MutableList<CodeVal<*>>
typealias Args = MutableMap<String, MutableList<CodeVal<*>>>

class CodeInst(
    val type: InstType,
    val rawArgs: RawArgs,
    val target: EventTarget<*> = TargetNone,
) {
    val args: Args = mutableMapOf()
    init {
        val params = type.params
        for (i in params.indices) {
            if (i == rawArgs.size) rawArgs.add(NULL_VALUE)
            try { params[i].compute(ParamNodeResult(0, params.lastIndex, rawArgs, args)) } catch (e: Exception) {
                throw RuntimeException("Compile-time check failed for param ${i+1}! ${type.name}", e)
            }
        }
    }
}
