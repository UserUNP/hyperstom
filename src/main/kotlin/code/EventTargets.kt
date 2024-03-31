@file:Suppress("UnstableApiUsage")

package dev.bedcrab.hyperstom.code

import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerEntityInteractEvent
import net.minestom.server.event.trait.EntityInstanceEvent
import net.minestom.server.event.trait.InstanceEvent
import kotlin.reflect.KClass

fun getCodeTarget(name: String) = nameToEventTarget[name] ?: throw RuntimeException("Unsupported code target! $name")
private val nameToEventTarget = mutableMapOf<String, EventTarget<*>>()
val PLAYERS_ALL_TARGET = EventTarget("PLAYERS_ALL", InstanceEvent::class) { event.instance.players }
val PLAYER_RAND_TARGET = EventTarget("PLAYER_RAND", InstanceEvent::class) { setOf(PLAYERS_ALL_TARGET(this).random()) }
val NPC_ALL_TARGET = EventTarget("NPC_ALL", InstanceEvent::class) { event.instance.entities.filter { it !is Player }.toSet() }
val NPC_RAND_TARGET = EventTarget("NPC_RAND", InstanceEvent::class) { setOf(NPC_ALL_TARGET(this).random()) }
val DEFAULT_TARGET = EventTarget("DEFAULT", EntityInstanceEvent::class) { setOf(event.entity) }

val ENTITY_CLICKED_TARGET = EventTarget("ENTITY_CLICKED", PlayerEntityInteractEvent::class) { setOf(event.entity) }

data class EventTarget<T : InstanceEvent>(val name: String, private val eventType: KClass<T>, private val get: EventInvokedContext<T>.() -> Set<Entity>) {
    operator fun invoke(ctx: EventInvokedContext<T>) = get(ctx)
    operator fun invoke(ctx: InvokeContext): Set<Entity> {
        val event = ctx.msEvent
        if (eventType.isInstance(event)) throw RuntimeException("Event ${event::class.simpleName} is not assignable to ${eventType.simpleName}")
        @Suppress("UNCHECKED_CAST") val getTargetCtx = EventInvokedContext(event as T, ctx.world)
        return get(getTargetCtx)
    }

    init { nameToEventTarget[name] = this }
}
