@file:Suppress("UnstableApiUsage")

package userunp.hyperstom

import net.minestom.server.event.Event
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.event.trait.EntityInstanceEvent
import userunp.hyperstom.code.EVENT_PLAYER_CHAT
import userunp.hyperstom.code.HSEvent
import userunp.hyperstom.code.eventLabel
import userunp.hyperstom.datastore.*
import userunp.hyperstom.world.WorldManager
import userunp.hyperstom.world.readWorldCode
import kotlin.reflect.KClass

fun initWorldModes(world: WorldManager, parentNode: EventNode<Event>) {
    parentNode.addChild(PlayHandler(world).node)
    parentNode.addChild(BuildHandler(world).node)
    parentNode.addChild(DevHandler(world).node)
}

enum class WorldMode { PLAY, BUILD, DEV }
interface ModeHandler {
    val node: EventNode<*>
}

private class PlayHandler(val world: WorldManager) : ModeHandler {
    override val node = EventNode.tag(
        "${world.id}:PLAY", EventFilter.ENTITY,
        TagStore.tag(StorePlayerState::class), ::inPlayMode
    )
    val code = readWorldCode(world)
    init {
        listener(PlayerChatEvent::class, EVENT_PLAYER_CHAT)
    }

    private fun <T : EntityInstanceEvent> listener(type: KClass<T>, event: HSEvent<T>) {
        node.addListener(type.java) {
            code(it, eventLabel(event), world)
        }
    }
}

private class BuildHandler(world: WorldManager) : ModeHandler {
    override val node = EventNode.tag(
        "${world.id}:BUILD", EventFilter.INSTANCE,
        TagStore.tag(StorePlayerState::class), ::inBuildMode
    )
}

private class DevHandler(world: WorldManager) : ModeHandler {
    override val node = EventNode.tag(
        "${world.id}:DEV", EventFilter.INSTANCE,
        TagStore.tag(StorePlayerState::class), ::inDevMode
    )
    init {
        node.addListener(PlayerBlockBreakEvent::class.java) { TODO("Code block placing is not done.") }
        node.addListener(PlayerBlockPlaceEvent::class.java) { TODO("Code block placing is not done.") }
    }
}
