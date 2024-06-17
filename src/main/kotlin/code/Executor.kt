@file:Suppress("UnstableApiUsage")

package userunp.hyperstom.code

import kotlinx.coroutines.*
import userunp.hyperstom.world.WorldManager
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.event.trait.InstanceEvent
import userunp.hyperstom.datastore.StoreWorldCode
import kotlin.reflect.KClass
import kotlin.reflect.cast

interface Invokable {
    val name: String
    fun check(ctx: InvokeContext)
}

interface Invoker {  //TODO: a debug invoker for debugging, and maybe misc invokers for fun
    val scope: CoroutineScope
    fun exec(ctx: InvokeContext, code: StoreWorldCode, startIndex: Int)
}

data class InvokeContext(
    val invokable: Invokable,
    val msEvent: InstanceEvent,
    val instList: InstList,
    val label: InstListLabel
)

class RuntimeInvoker(val world: WorldManager) : Invoker {
    override val scope = CoroutineScope(Dispatchers.Default)
    override fun exec(ctx: InvokeContext, code: StoreWorldCode, startIndex: Int) {
        scope.launch(CoroutineName("${world.id}:${ctx.invokable.name}")) {
            val controller = ExecController(ctx, code, startIndex)
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

data class ExecFrame(
    val label: InstListLabel,
    val instList: InstList,
    var instIndex: Int,
) //TODO: variables here, & labels for jump type execution flows

class ExecController(ctx: InvokeContext, private val code: StoreWorldCode, startIndex: Int) {
    private var previous: ExecFrame? = null
    var frame = ExecFrame(ctx.label, ctx.instList, startIndex)

    operator fun iterator() = this
    operator fun next() = frame.instList[frame.instIndex++]
    operator fun hasNext(): Boolean {
        val hasNext = frame.instIndex <= frame.instList.size-1
        if (!hasNext) previous?.let {
            previous = null
            frame = it
            return@hasNext hasNext()
        }
        return hasNext
    }

    fun jumpTo(label: InstListLabel, startIndex: Int) {
        val instList = code.getInstList(label) ?: throw RuntimeException("Could not find label to jump to! ${label.name}")
        previous = frame
        frame = ExecFrame(label, instList, startIndex)
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

val EVENT_WORLD_INIT = HSEvent("WORLD_INIT", InstanceEvent::class, values = setOf(EVENT_VAL_WORLD_NAME))
val EVENT_PLAYER_CHAT = HSEvent("PLAYER_CHAT", PlayerChatEvent::class, targets = setOf(TARGET_DEFAULT))

data class HSEvent<T : InstanceEvent>(
    override val name: String,
    val baseEventType: KClass<T>,
    val values: Set<EventValue<*, *>> = setOf(),
    val targets: Set<EventTarget<*>> = setOf(),
) : Invokable {
    init { nameToHSEvent[name] = this }
    override fun check(ctx: InvokeContext) {} //TODO: disallow unsupported actions in certain events
}

data class HSProcess(override val name: String) : Invokable {
    override fun check(ctx: InvokeContext) {} //TODO: process parameters & check here
}
