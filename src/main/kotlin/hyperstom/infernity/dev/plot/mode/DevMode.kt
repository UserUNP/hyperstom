package hyperstom.infernity.dev.plot.mode

import hyperstom.infernity.dev.code.block.CodeBlock
import hyperstom.infernity.dev.tagstore.StorePlotState
import hyperstom.infernity.dev.tagstore.TagStore
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent

object DevMode : ModeHandler {
    private val node = EventNode.tag("modeHandler_dev", EventFilter.PLAYER, TagStore.tag(StorePlotState::class), StorePlotState::usingDev)

    override fun init() {
        node.addListener(PlayerBlockPlaceEvent::class.java, this::placeBlock)
        node.addListener(PlayerBlockBreakEvent::class.java, this::breakBlock)
    }

    private fun placeBlock(event: PlayerBlockPlaceEvent) {
        val cBlockProps = CodeBlock.get(event.block)
        if (cBlockProps == null) {
            event.isCancelled = true
            return
        }
        val cBlock = cBlockProps.default()
        cBlock.place(event.blockPosition, event.player.instance,  cBlock.defaultSign.block)
        // I removed the code for pushing blocks to get this out quicker
        // also the defaultSign() method is for when player resets a data block's first line
        // the entire block should be placed again and not only the sign so there's not a bunch of placement errors
    }

    private fun breakBlock(event: PlayerBlockBreakEvent) {
        event.isCancelled = true
    }

    override fun getNode(): EventNode<*> = node
    override fun toString(): String = "Dev"
}
