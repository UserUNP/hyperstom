@file:Suppress("UnstableApiUsage")

package ma.userunp.hyperstom

import ma.userunp.hyperstom.code.CodeEvent
import ma.userunp.hyperstom.code.eventLabel
import ma.userunp.hyperstom.world.WorldCode
import ma.userunp.hyperstom.world.WorldManager
import net.minestom.server.event.Event
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.event.trait.EntityInstanceEvent
import kotlin.reflect.KClass

interface ModeHandler {
    val node: EventNode<*>
}

enum class WorldMode(val get: (WorldManager) -> ModeHandler) {
    PLAY(::PlayHandler),
    BUILD(::BuildHandler),
}

fun initWorldModes(world: WorldManager, parentNode: EventNode<Event>) {
    for (m in WorldMode.entries) parentNode.addChild(m.get(world).node)
}

private class PlayHandler(val world: WorldManager) : ModeHandler {
    override val node = EventNode.value("${world.id}:PLAY", EventFilter.ENTITY) {
        playerStates[it.uuid]?.mode == WorldMode.PLAY
    }

    init { world.files.code.let {
        listener(it, PlayerChatEvent::class, CodeEvent.CHAT)
    } }

    private fun <T : EntityInstanceEvent> listener(code: WorldCode, type: KClass<T>, event: CodeEvent) {
        node.addListener(type.java) {
            //TODO: that world specific logs debug thingy, debug log info about the event getting fired
            code.invoke(it, eventLabel(event), world.runtimeInvoker)
        }
    }
}

private class BuildHandler(world: WorldManager) : ModeHandler {
    override val node = EventNode.value("${world.id}:BUILD", EventFilter.PLAYER) {
        playerStates[it.uuid]?.mode == WorldMode.BUILD
    }
}

class PlayerState(var mode: WorldMode, var world: WorldManager)
