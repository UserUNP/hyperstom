@file:Suppress("UnstableApiUsage")

package userunp.hyperstom.code

import kotlinx.serialization.Serializable
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerEntityInteractEvent
import net.minestom.server.event.trait.EntityInstanceEvent
import net.minestom.server.event.trait.InstanceEvent
import userunp.hyperstom.IdentifiableSerializer
import kotlin.reflect.KClass

fun getEventTarget(name: String) = nameToEventTarget[name] ?: throw RuntimeException("Unsupported code target! $name")
private val nameToEventTarget = mutableMapOf<String, EventTarget<*>>()

class EntityAsAudience(val entity: Entity) : Audience
@Suppress("OverrideOnly") fun ForwardingAudience.entities() = audiences().filterIsInstance<EntityAsAudience>()

private object EventTargetSerializer : IdentifiableSerializer<EventTarget<*>>(::getEventTarget)
@Serializable(EventTargetSerializer::class) class EventTarget<T : InstanceEvent>(
    name: String,
    eventType: KClass<T>,
    get: EventDataContext<T>.() -> Set<Audience>,
) : EventDataProcessor<T, Set<Audience>>(name, eventType, get)

// non-event specific
val TARGET_NONE = reg("NONE", InstanceEvent::class) { setOf() }
val TARGET_PLAYERS_ALL = reg("PLAYERS_ALL", InstanceEvent::class) { event.instance.players }
val TARGET_PLAYER_RAND = reg("PLAYER_RAND", InstanceEvent::class) { setOf(TARGET_PLAYERS_ALL.get(this).random()) }
val TARGET_NPC_ALL = reg("NPC_ALL", InstanceEvent::class) {
    event.instance.entities.mapNotNull {if (it !is Player) EntityAsAudience(it) else null }.toSet()
}
val TARGET_NPC_RAND = reg("NPC_RAND", InstanceEvent::class) { setOf(TARGET_NPC_ALL.get(this).random()) }
val TARGET_DEFAULT = reg("DEFAULT", EntityInstanceEvent::class) { setOf(EntityAsAudience(event.entity)) }
// event specific
val TARGET_ENTITY_CLICKED = reg("ENTITY_CLICKED", PlayerEntityInteractEvent::class) { setOf(event.entity) }

private fun <T : InstanceEvent> reg(n: String, e: KClass<T>, g: EventDataContext<T>.() -> Set<Audience>)
    = EventTarget(n, e, g).also { nameToEventTarget[n] = it }

fun EventTarget<*>.targetClass() =
    if (name == "ALL") TargetClass.ALL
    else if (name.startsWith("PLAYER")) TargetClass.PLAYER
    else if (name.startsWith("NPC")) TargetClass.NPC
    else TargetClass.NONE

enum class TargetClass {
    NONE, ALL, PLAYER, NPC;

    fun worksWith(other: TargetClass) = this == ALL || other == ALL || this == other
}
