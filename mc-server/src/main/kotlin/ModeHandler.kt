package dev.bedcrab.hyperstom

import dev.bedcrab.hyperstom.code.*
import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.coordinate.Point
import net.minestom.server.event.Event
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block

private val LOGGER = KotlinLogging.logger {}

fun initModeHandlers(parentNode: EventNode<in Event>) {
    for (mode in ModeHandler.Mode.entries) {
        mode.init()
        parentNode.addChild(mode.eventNode)
    }
}

interface ModeHandler {
    val eventNode: EventNode<*>
    fun init()

    enum class Mode(private val handler: ModeHandler) : ModeHandler {
        PLAY(PlayMode),
        BUILD(BuildMode),
        DEV(DevMode),
        ;
        override val eventNode = handler.eventNode
        override fun init() {
            handler.init()
            LOGGER.info { "$name mode initialized." }
        }
        override fun toString() = handler.toString()
    }
}

private object PlayMode : ModeHandler {
    // entities should have the tag to be considered a part of the plot anyway
    override val eventNode = EventNode.tag(
        "modeHandler_play", EventFilter.ENTITY,
        TagStore.tag(StorePlayerState::class), StorePlayerState::usingPlay
    )
    override fun init() {
        eventNode.addListener(PlayerBlockPlaceEvent::class.java) { it.isCancelled = true }
        eventNode.addListener(PlayerBlockBreakEvent::class.java) { it.isCancelled = true }
    }
}

private object BuildMode : ModeHandler {
    override val eventNode = EventNode.tag(
        "modeHandler_build", EventFilter.PLAYER,
        TagStore.tag(StorePlayerState::class), StorePlayerState::usingBuild
    )
    override fun init() {}
}

private object DevMode : ModeHandler {
    override val eventNode = EventNode.tag(
        "modeHandler_dev", EventFilter.PLAYER,
        TagStore.tag(StorePlayerState::class), StorePlayerState::usingDev
    )
    override fun init() {
        eventNode.addListener(PlayerBlockPlaceEvent::class.java) { try { placeBlock(it) } catch (e: Exception) {
            it.player.sendMessage("ERROR: ${e.message}")
            it.isCancelled = true
        } }
        eventNode.addListener(PlayerBlockBreakEvent::class.java) { try { breakBlock(it) } catch (e: Exception) {
            it.player.sendMessage("ERROR: ${e.message}")
            it.isCancelled = true
        } }
    }

    private fun placeBlock(event: PlayerBlockPlaceEvent) {
        event.isCancelled = true
        val instance = event.player.instance
        val pos = event.blockPosition
        val msBlock = event.block

        checkMinestomBlockPlacement(instance, pos)
        val hsBlock = getCodeMinestomBlock(msBlock)
        val (rootPos, rootBlock) = findMinestomRootBlock(pos, instance) ?: (null to null)
        // TODO: world code data
        if (rootPos != null && rootBlock != null) {
            if (hsBlock.type.root) throw RuntimeException("Invalid block placement!")
            val endBracketPos = findMinestomEndBracket(pos, instance) ?: throw RuntimeException("Invalid block placement!")
            move(instance, getConnectorMinestomPos(pos), endBracketPos, hsBlock.type.space)
        } else if (!hsBlock.type.root) throw RuntimeException("Invalid block placement: expected an event/data block!")
        instance.placeCodeBlock(msBlock, hsBlock, null, pos)
    }

    private fun breakBlock(event: PlayerBlockBreakEvent) {
        event.isCancelled = true
        val block: CodeBlock = try { getCodeMinestomBlock(event.block) } catch (_: Exception) { return }
        val instance = event.player.instance
        val pos = event.blockPosition

        instance.setBlock(pos, Block.AIR)
        val connectorPos = getConnectorMinestomPos(pos)
        val connectorBlock = instance.getBlock(connectorPos)
        val connectorIsBracket = connectorBlock == OPEN_BRACKET_MS_BLOCK
        if (connectorBlock == CONNECTOR_MS_BLOCK || connectorIsBracket) instance.setBlock(connectorPos, Block.AIR)
        instance.setBlock(getContainerMinestomPos(pos), Block.AIR)
        instance.setBlock(getInstVisMinestomPos(pos), Block.AIR)

        if (connectorIsBracket) {
            val endBracketPos = findMinestomEndBracket(connectorPos, instance) ?: throw RuntimeException("Couldn't find the ending bracket")
            instance.setBlock(endBracketPos, Block.AIR)
            move(instance, connectorPos, endBracketPos, -block.type.space)
        }
    }

    private fun move(instance: Instance, start: Point, end: Point, amount: Int) {
        for (z in start.blockZ()..end.blockZ()) {
            val currentPos = start.withZ(z.toDouble())
            val msBlock = instance.getBlock(currentPos)
            if (msBlock == Block.AIR) continue
            // position & block
            val upPos = getContainerMinestomPos(currentPos)
            val containerBlock = instance.getBlock(upPos)
            val leftPos = getInstVisMinestomPos(currentPos)
            val instVisBlock = instance.getBlock(leftPos)
            instance.setBlock(currentPos, Block.AIR)
            instance.setBlock(upPos, Block.AIR)
            instance.setBlock(leftPos, Block.AIR)
            // move backwards
            instance.setBlock(currentPos.withZ { it + amount }, msBlock)
            instance.setBlock(upPos.withZ { it + amount }, containerBlock)
            instance.setBlock(leftPos.withZ { it + amount }, instVisBlock)
        }
    }
}
