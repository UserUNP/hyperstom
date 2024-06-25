@file:Suppress("UnstableApiUsage")

package userunp.hyperstom.code

import kotlinx.serialization.Serializable
import userunp.hyperstom.MM
import net.minestom.server.event.trait.EntityInstanceEvent
import net.minestom.server.event.trait.InstanceEvent
import userunp.hyperstom.IdentifiableSerializer
import kotlin.reflect.KClass

fun getEventVal(name: String) = nameToEventVal[name] ?: throw RuntimeException("Unsupported code target! $name")
private val nameToEventVal = mutableMapOf<String, EventVal<*, *>>()

private object EventValueSerializer : IdentifiableSerializer<EventVal<*, *>>(::getEventVal)
@Serializable(EventValueSerializer::class) class EventVal<T : InstanceEvent, S : CodeValBox>(
    name: String,
    eventType: KClass<T>,
    get: EventDataContext<T>.() -> CodeVal<S>
) : EventDataProcessor<T, CodeVal<S>>(name, eventType, get)

val EVENT_VAL_WORLD_TITLE  = reg("WORLD_NAME", InstanceEvent::class) { CodeVal(VAL_TYPE_TXT, TxtVal(MM.deserialize(world.info.title))) }
val EVENT_VAL_ENTITY_UUID  = reg("ENTITY_UUID", EntityInstanceEvent::class) { CodeVal(VAL_TYPE_STR, StrVal(event.entity.uuid.toString())) }

private fun <T : InstanceEvent, S : CodeValBox> reg(n: String, e: KClass<T>, g: EventDataContext<T>.() -> CodeVal<S>)
    = EventVal(n, e, g).also { nameToEventVal[n] = it }
