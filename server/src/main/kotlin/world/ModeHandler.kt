package dev.bedcrab.hyperstom.world

import dev.bedcrab.hyperstom.*
import dev.bedcrab.hyperstom.code.CONNECTOR_MS_BLOCK
import dev.bedcrab.hyperstom.code.CodeBlock
import dev.bedcrab.hyperstom.code.isOpenBracket
import dev.bedcrab.hyperstom.datastore.StorePlayerState
import dev.bedcrab.hyperstom.datastore.TagStore
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

        val block = CodeBlock.from(event.block)
        if (instance.getBlock(shiftPoint(pos, RIGHT_VEC)) != Block.AIR) {
            throw RuntimeException("Invalid block placement!")
        }
        val (rootPos, rootBlock) = findRootBlock(pos, instance) ?: (null to null)
        // TODO: world code data
        if (rootPos != null && rootBlock != null) {
            if (block.type.root) throw RuntimeException("Invalid block placement!")
            val endBracketPos = findEndBracket(pos, instance) ?: throw RuntimeException("Invalid block placement!")
            move(instance, shiftPoint(pos, FORWARD_VEC), endBracketPos, block.type.space)
        } else if (!block.type.root) throw RuntimeException("Invalid block placement: expected an event/data block!")
        block.place(instance,  pos, null)
    }

    private fun breakBlock(event: PlayerBlockBreakEvent) {
        event.isCancelled = true
        val block: CodeBlock = try { CodeBlock.from(event.block) } catch (_: Exception) { return }
        val instance = event.player.instance
        val pos = event.blockPosition

        instance.setBlock(pos, Block.AIR)
        val connectorPos = shiftPoint(pos, FORWARD_VEC)
        val connectorBlock = instance.getBlock(connectorPos)
        val connectorIsBracket = isOpenBracket(connectorBlock)
        if (connectorBlock == CONNECTOR_MS_BLOCK || connectorIsBracket) instance.setBlock(connectorPos, Block.AIR)
        instance.setBlock(shiftPoint(pos, UP_VEC), Block.AIR)
        instance.setBlock(shiftPoint(pos, LEFT_VEC), Block.AIR)

        if (connectorIsBracket) {
            val endBracketPos = findEndBracket(connectorPos, instance)
            if (endBracketPos == null) {
                event.player.sendMessage("WARNING: Couldn't find the ending bracket!")
                return
            }
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
            val upPos = shiftPoint(currentPos, UP_VEC)
            val containerBlock = instance.getBlock(upPos)
            val leftPos = shiftPoint(currentPos, LEFT_VEC)
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
