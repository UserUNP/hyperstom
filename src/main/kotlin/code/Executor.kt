@file:Suppress("UnstableApiUsage")

package userunp.hyperstom.code

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import userunp.hyperstom.world.WorldManager
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.event.trait.InstanceEvent
import kotlin.reflect.KClass
import kotlin.reflect.cast

interface Invokable {
    val name: String
    fun check(ctx: InvokeContext)
}

/* TODO:
find out how to implement a minestom scheduler utilizing kotlin coroutines
because trying to run asynchronously outside the tick thread isn't the best idea
 */

data class InvokeContext(
    val invokable: Invokable,
    val msEvent: InstanceEvent,
    val instList: InstList,
    val label: CodeLabel
)

interface Invoker {  //TODO: a debug invoker for debugging, and maybe misc invokers for fun
    val scope: CoroutineScope
    fun exec(ctx: InvokeContext, startIndex: Int, labelResolver: CodeLabelResolver)
}

class RuntimeInvoker(val world: WorldManager) : Invoker {
    override val scope = CoroutineScope(Dispatchers.Default)
    override fun exec(ctx: InvokeContext, startIndex: Int, labelResolver: CodeLabelResolver) {
        scope.launch(CoroutineName("${world.id}:${ctx.invokable.name}")) {
            val controller = ExecController(ctx, startIndex, labelResolver)
            try {
                for (inst in controller) {
                    val targetClass = inst.target.targetClass()
                    if (!targetClass.worksWith(inst.props.targetClass)) throw RuntimeException(
                        "Cannot use target with class $targetClass against the instruction ${inst.props.name}!"
                    )
                    inst(controller, ctx.msEvent.instance, inst.target.get(world, ctx))
                }
            } catch(e: Exception) { throw RuntimeException("Runtime exception.", e) }
        }
    }
}

fun eventLabel(e: HSEvent<*>) = CodeLabel(CodeBlockType.EVENT, e.name)
fun dataLabel(name: String) = CodeLabel(CodeBlockType.DATA, name)
fun scopedLabel(name: String) = CodeLabel(CodeBlockType.SCOPED, name)

@Serializable data class CodeLabel(val type: CodeBlockType, override val name: String) : CodeValBox

fun interface CodeLabelResolver {
    fun resolveLabel(label: CodeLabel): InstList
}

fun interface CodeVarResolver {
    fun resolveVar(name: String): CodeVal<*>
}

typealias CodeVars = MutableMap<String, CodeVal<*>>

data class ExecFrame(
    val label: CodeLabel,
    val instList: InstList,
    var instIndex: Int,
    val vars: CodeVars = mutableMapOf(),
    var previous: ExecFrame? = null,
)

class ExecController(
    ctx: InvokeContext,
    startIndex: Int,
    private val labelResolver: CodeLabelResolver,
) : CodeVarResolver {
    var frame = ExecFrame(ctx.label, ctx.instList, startIndex)
    private lateinit var currInst: Instruction

    operator fun next(): Instruction {
        currInst = frame.instList[frame.instIndex++]
        return currInst
    }

    operator fun hasNext(): Boolean {
        val hasNext = frame.instIndex <= frame.instList.size-1
        if (!hasNext) frame.previous?.let {
            frame = it
            return@hasNext hasNext()
        }
        return hasNext
    }

    operator fun iterator() = this

    fun jumpTo(label: CodeLabel, startIndex: Int) {
        val instList = labelResolver.resolveLabel(label)
        frame = ExecFrame(label, instList, startIndex, previous = frame)
    }

    override fun resolveVar(name: String) = frame.vars[name]
        ?: (if (frame.label.type == CodeBlockType.SCOPED) frame.previous?.let { it.vars[name] } else null)
        ?: throw RuntimeException("No such var! $name")

    fun argAny(p: Parameter<*>): CodeVal<*> {
        val codeVal = currInst.args[p.name] ?: throw RuntimeException("No such argument! ${p.name}")
        return when (codeVal.type) {
            VAL_TYPE_VAR -> resolveVar((codeVal.value as StrVal).name)
            else -> codeVal
        }
    }

    fun <T : CodeValBox> arg(p: Parameter<T>): CodeVal<T> {
        if (p.type == null) throw RuntimeException("Cannot get argument for parameter with a null type! ${p.name}")
        val arg = argAny(p)
        if (p.type != arg.type) throw RuntimeException("Expected ${p.type.name}, got ${arg.type.name} instead! ${p.name}")
        @Suppress("UNCHECKED_CAST")
        return arg as CodeVal<T>
    }
}

data class EventDataContext<T : InstanceEvent>(val event: T, val world: WorldManager)
abstract class EventDataProcessor<T : InstanceEvent, out S>(
    override val name: String,
    private val eventType: KClass<T>,
    private val getter: EventDataContext<T>.() -> S
) : CodeValBox {
    fun get(ctx: EventDataContext<T>) = getter(ctx)
    fun get(world: WorldManager, ctx: InvokeContext) = getter(EventDataContext(eventType.cast(ctx.msEvent), world))
}

fun getEvents() = nameToHSEvent.values as Collection<HSEvent<*>>
fun getEvent(name: String) = nameToHSEvent[name] ?: throw RuntimeException("Unsupported event value! $name")
private val nameToHSEvent = mutableMapOf<String, HSEvent<*>>()

val EVENT_WORLD_INIT = HSEvent("WORLD_INIT", InstanceEvent::class, values = setOf(EVENT_VAL_WORLD_TITLE))
val EVENT_PLAYER_CHAT = HSEvent("PLAYER_CHAT", PlayerChatEvent::class, targets = setOf(TARGET_DEFAULT))

data class HSEvent<T : InstanceEvent>(
    override val name: String,
    val baseEventType: KClass<T>,
    val values: Set<EventVal<*, *>> = setOf(),
    val targets: Set<EventTarget<*>> = setOf(),
) : Invokable {
    init { nameToHSEvent[name] = this }
    override fun check(ctx: InvokeContext) {} //TODO: disallow unsupported actions in certain events
}

data class HSProcess(override val name: String) : Invokable {
    override fun check(ctx: InvokeContext) {} //TODO: process parameters & check here
}
