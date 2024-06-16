@file:Suppress("UnstableApiUsage")

package userunp.hyperstom.code

import kotlinx.serialization.Serializable
import userunp.hyperstom.MM
import net.minestom.server.event.trait.EntityInstanceEvent
import net.minestom.server.event.trait.InstanceEvent
import userunp.hyperstom.IdentifiableSerializer
import kotlin.reflect.KClass

fun getEventVal(name: String) = nameToEventVal[name] ?: throw RuntimeException("Unsupported code target! $name")
private val nameToEventVal = mutableMapOf<String, EventValue<*, *>>()

private object EventValueSerializer : IdentifiableSerializer<EventValue<*, *>>(::getEventVal)
@Serializable(EventValueSerializer::class) class EventValue<T : InstanceEvent, S : CodeValBox>(
    name: String,
    eventType: KClass<T>,
    get: EventDataContext<T>.() -> CodeValue<S>
) : EventDataProcessor<T, CodeValue<S>>(name, eventType, get)

val EVENT_VAL_WORLD_NAME  = reg("WORLD_NAME", InstanceEvent::class) { CodeValue(VALUE_TYPE_TEXT, TextVal(MM.deserialize(world.info.name))) }
val EVENT_VAL_ENTITY_UUID  = reg("ENTITY_UUID", EntityInstanceEvent::class) { CodeValue(VALUE_TYPE_STR, StrVal(event.entity.uuid.toString())) }

private fun <T : InstanceEvent, S : CodeValBox> reg(n: String, e: KClass<T>, g: EventDataContext<T>.() -> CodeValue<S>)
    = EventValue(n, e, g).also { nameToEventVal[n] = it }
