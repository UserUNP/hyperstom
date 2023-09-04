package hyperstom.infernity.dev.plot.mode

import hyperstom.infernity.dev.tagstore.StorePlotState
import hyperstom.infernity.dev.tagstore.TagStore
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode

object BuildMode : ModeHandler {
    private val node = EventNode.tag("modeHandler_build", EventFilter.PLAYER, TagStore.tag(StorePlotState::class), StorePlotState::usingBuild)

    override fun init() {
    }

    override fun getNode(): EventNode<*> = node
    override fun toString(): String = "Build"
}
