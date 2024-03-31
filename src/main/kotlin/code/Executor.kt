@file:OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)

package dev.bedcrab.hyperstom.code

import dev.bedcrab.hyperstom.world.WorldManager
import kotlinx.coroutines.*
import net.minestom.server.event.trait.InstanceEvent
import net.minestom.server.instance.Instance
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass

class ExecutionController {
    val scope = CoroutineScope(EmptyCoroutineContext)
    val events = mutableMapOf<HSEvent, MutableSet<Job>>()
    //TODO: val processes = mutableMapOf<HSProcess, MutableSet<Job>>()
}

data class ExecContext(val instList: InstList, val inst: Instruction, val instance: Instance)
typealias InstFunction = ExecContext.() -> Unit
typealias Invokable = (InvokeContext) -> ExecutionController
data class InvokeContext(
    val world: WorldManager,
    val msEvent: InstanceEvent,
    val instructions: InstList,
)
data class EventInvokedContext<T : InstanceEvent>(val event: T, val world: WorldManager)

fun getEvents() = nameToHSEvent.values as Collection<HSEvent>
fun getEvent(name: String) = nameToHSEvent[name] ?: throw RuntimeException("Unsupported event value! $name")
private val nameToHSEvent = mutableMapOf<String, HSEvent>()
val WORLD_INIT_EVENT = HSEvent("WORLD_INITIALIZATION", InstanceEvent::class, setOf(WORLD_NAME_EVENT_VAL))

data class HSEvent(
    val name: String,
    val baseEventType: KClass<*>,
    val eventValues: Set<EventValue<*>> = setOf(),
    val eventTargets: Set<EventTarget<*>> = setOf(),
    /* TODO: disallow unsupported actions in certain events */
) : Invokable {
    override fun toString() = name
    init { nameToHSEvent[name] = this }

    override operator fun invoke(ctx: InvokeContext) = ExecutionController().apply {
        (events[this@HSEvent] ?: mutableSetOf<Job>().also { events[this@HSEvent] = it })
            .add(scope.launch(newSingleThreadContext("${ctx.world.id}:$name")) {
                try {
                    val computedEventValues = eventValues.associateWith { it(ctx) }
                    val computedEventTargets = eventTargets.associateWith { it(ctx) }
                    for (inst in ctx.instructions) inst(ctx.msEvent.instance, ctx.instructions)
                } catch (e: Exception) {
                    throw RuntimeException("Runtime exception.", e)
                }
            })
    }
}
