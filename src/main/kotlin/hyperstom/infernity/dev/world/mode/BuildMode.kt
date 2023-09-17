package hyperstom.infernity.dev.world.mode

import hyperstom.infernity.dev.tagstore.StoreWorldState
import hyperstom.infernity.dev.tagstore.TagStore
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode

object BuildMode : ModeHandler {
    override val eventNode = EventNode.tag("modeHandler_build", EventFilter.PLAYER, TagStore.tag(StoreWorldState::class), StoreWorldState::usingBuild)

    override fun init() {
    }

}
