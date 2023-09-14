package hyperstom.infernity.dev.world.mode

import hyperstom.infernity.dev.tagstore.StoreWorldState
import hyperstom.infernity.dev.tagstore.TagStore
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent

object PlayMode : ModeHandler {
    private val node = EventNode.tag("modeHandler_play", EventFilter.ENTITY, TagStore.tag(StoreWorldState::class), StoreWorldState::usingPlay)
    // entities should have the tag to be considered a part of the plot anyway

    override fun init() {
        node.addListener(PlayerBlockPlaceEvent::class.java) { it.isCancelled = true }
        node.addListener(PlayerBlockBreakEvent::class.java) { it.isCancelled = true }
    }

    override fun getNode(): EventNode<*> = node
    override fun toString(): String = "Play"
}
