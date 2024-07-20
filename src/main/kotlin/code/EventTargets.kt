@file:Suppress("UnstableApiUsage")

package ma.userunp.hyperstom.code

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.minestom.server.entity.Entity
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerEntityInteractEvent
import net.minestom.server.event.trait.EntityInstanceEvent
import net.minestom.server.event.trait.InstanceEvent
import kotlin.reflect.KClass

private fun <T : InstanceEvent> implTarget(n: String, e: KClass<T>, g: (T) -> Set<Audience>) = object : EventTarget<T> {
    override val name = n
    override val eventType = e
    override fun get(msEvent: T) = g(msEvent)
}

class NPCAsAudience(val entity: Entity) : Audience
@Suppress("OverrideOnly") fun ForwardingAudience.nonPlayers() = audiences().filterIsInstance<NPCAsAudience>()
@Suppress("OverrideOnly") fun ForwardingAudience.players() = audiences().filterIsInstance<Player>()
@Suppress("OverrideOnly") fun ForwardingAudience.livingEntities() = audiences().mapNotNull {
    if (it is LivingEntity) it else if (it is NPCAsAudience && it.entity is LivingEntity) it.entity else null
}

// non-event specific

object TargetNone : EventTarget<InstanceEvent> by implTarget("NONE",
    InstanceEvent::class,
    { setOf() },
)
object TargetPlayersAll : EventTarget<InstanceEvent> by implTarget("PLAYERS ALL",
    InstanceEvent::class,
    { it.instance.players },
)
object TargetPlayerRand : EventTarget<InstanceEvent> by implTarget("PLAYER RAND",
    InstanceEvent::class,
    { setOf(TargetPlayersAll.get(it).random()) },
)
object TargetNPCAll : EventTarget<InstanceEvent> by implTarget("NPC ALL",
    InstanceEvent::class,
    { it.instance.entities.mapNotNull { entity -> if (entity !is Player) NPCAsAudience(entity) else null }.toSet() },
)
object TargetNPCRand : EventTarget<InstanceEvent> by implTarget("NPC RAND",
    InstanceEvent::class,
    { setOf(TargetNPCAll.get(it).random()) },
)

// event specific

object TargetDefault : EventTarget<EntityInstanceEvent> by implTarget("DEFAULT",
    EntityInstanceEvent::class,
    { setOf(if (it.entity is Audience) it.entity as Audience else NPCAsAudience(it.entity)) },
)
object TargetEntityClicked : EventTarget<PlayerEntityInteractEvent> by implTarget("ENTITY CLICKED",
    PlayerEntityInteractEvent::class,
    { setOf(it.entity) },
)

fun EventTarget<*>.targetClass() =
    if (name == "NONE") TargetClass.NONE
    else if (name.startsWith("PLAYER")) TargetClass.PLAYER
    else if (name.startsWith("NPC")) TargetClass.NPC
    else TargetClass.ALL

enum class TargetClass {
    NONE, ALL, PLAYER, NPC;

    fun worksWith(other: TargetClass) = this == ALL || other == ALL || this == other
}
