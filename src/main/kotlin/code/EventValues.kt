package dev.bedcrab.hyperstom.code

import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.event.trait.InstanceEvent
import kotlin.reflect.KClass

private val LOGGER = KotlinLogging.logger {}

fun getEventVal(name: String) = nameToEventVal[name] ?: throw RuntimeException("Unsupported code target! $name")
private val nameToEventVal = mutableMapOf<String, EventValue<*>>()
val WORLD_NAME_EVENT_VAL  = EventValue("WORLD_NAME", InstanceEvent::class) { CodeValueEntry(TEXT_VALUE_TYPE.name, world.info.name) }

data class EventValue<T : InstanceEvent>(val name: String, private val eventType: KClass<T>, private val get: EventInvokedContext<T>.() -> CodeValueEntry) {
    operator fun invoke(ctx: EventInvokedContext<T>) = get(ctx)
    operator fun invoke(ctx: InvokeContext): CodeValueEntry {
        val event = ctx.msEvent
        if (eventType.isInstance(event)) throw RuntimeException("Event ${event::class.simpleName} is not assignable to ${eventType.simpleName}")
        @Suppress("UNCHECKED_CAST") val getEventValCtx = EventInvokedContext(event as T, ctx.world)
        return get(getEventValCtx)
    }

    init {
        nameToEventVal[name] = this
        LOGGER.info { "Registered event value type $name@${hashCode()}" }
    }
}
