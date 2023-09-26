package hyperstom.infernity.dev.world.mode

import hyperstom.infernity.dev.Utils
import hyperstom.infernity.dev.tagstore.StoreWorldState
import hyperstom.infernity.dev.tagstore.TagStore
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.tag.Tag
import net.minestom.server.utils.Direction

object DevMode : ModeHandler {
    override val eventNode = EventNode.tag("modeHandler_dev", EventFilter.PLAYER, TagStore.tag(StoreWorldState::class), StoreWorldState::usingDev)

    override fun init() {
        eventNode.addListener(PlayerBlockPlaceEvent::class.java, this::placeBlock)
        eventNode.addListener(PlayerBlockBreakEvent::class.java, this::breakBlock)
    }

    private fun placeBlock(event: PlayerBlockPlaceEvent) {
        event.isCancelled = true

        val props = ActionCodeBlock.get(event.block)
        if (props == null) {
            event.isCancelled = true
            return
        }
        val block = props.default()
        try {
            if (event.player.instance.getBlock(Utils.shiftPoint(event.blockPosition, Direction.EAST)) != Block.AIR) throw RuntimeException("Invalid block placement!")
            block.place(event.blockPosition, event.player.instance,  block.defaultSign.block)
        } catch (e: Exception) {
            event.player.sendMessage("ERROR: ${e.message}")
            event.isCancelled = true
            return
        }
    }

    private fun breakBlock(event: PlayerBlockBreakEvent) {
        event.isCancelled = true
        if (!event.block.hasTag(Tag.String("codeblock"))) return

        val instance = event.player.instance
        val pos = event.blockPosition
        var validPlacement = true

        when (event.block.getTag(Tag.String("type"))) {
            "block" -> {
                instance.setBlock(pos, Block.AIR)
                var width = 1
                val connectorPos = Utils.shiftPoint(pos, Direction.SOUTH)
                val connectorBlock = instance.getBlock(connectorPos)
                if (connectorBlock == Block.STONE || (connectorBlock.name().endsWith("piston") && connectorBlock.getProperty("facing") == "south") ) {
                    instance.setBlock(connectorPos, Block.AIR)
                    width++
                }
                instance.setBlock(Utils.shiftPoint(pos, Direction.UP), Block.AIR)
                instance.setBlock(Utils.shiftPoint(pos, Direction.WEST), Block.AIR)

                if (!connectorBlock.name().endsWith("piston") || connectorBlock.getProperty("facing") != "south") return
                val pistonEndPos = Utils.findEndPiston(connectorPos, instance, connectorBlock == Block.STICKY_PISTON.withProperty("facing", "south"))
                if (pistonEndPos == null) {
                    event.player.sendMessage("WARNING: Couldn't find end piston!")
                    return
                }
                instance.setBlock(pistonEndPos, Block.AIR)

                for (z in connectorPos.blockZ()..<pistonEndPos.blockZ()) {
                    val currentPos = connectorPos.withZ(z.toDouble())
                    val upPos = Utils.shiftPoint(currentPos, Direction.UP)
                    val signPos = Utils.shiftPoint(currentPos, Direction.WEST)
                    val block = instance.getBlock(currentPos)
                    if (block == Block.AIR) continue
                    val upBlock = instance.getBlock(upPos)
                    val signBlock = instance.getBlock(signPos)
                    instance.setBlock(currentPos, Block.AIR)
                    instance.setBlock(upPos, Block.AIR)
                    instance.setBlock(signPos, Block.AIR)
                    instance.setBlock(currentPos.withZ { it - width }, block)
                    instance.setBlock(upPos.withZ { it - width }, upBlock)
                    instance.setBlock(signPos.withZ { it - width }, signBlock)
                }

                // val blocks = mutableListOf<Block>()
                // for (sx in x - 1..x + 1) {
                //     for (sy in y..y + 2) {
                //         for (sz in otherZ + 2..128) {
                //             blocks.add(event.player.instance.getBlock(sx, sy, sz))
                //             event.player.instance.setBlock(sx, sy, sz, Block.AIR)
                //         }
                //     }
                // }
                // val iter = blocks.iterator()
                // for (sx in x - 1..x + 1) for (sy in y..y + 2) for (sz in otherZ..126) {
                //     event.player.instance.setBlock(sx, sy, sz, iter.next())
                // }
            }
            else -> validPlacement = false

        }
        // if (validPlacement) {
        //     val blocks = mutableListOf<Block>()
        //     for (sx in x - 1..x + 1) {
        //         for (sy in y..y + 2) {
        //             for (sz in z + 2..128) {
        //                 blocks.add(event.player.instance.getBlock(sx, sy, sz))
        //                 event.player.instance.setBlock(sx, sy, sz, Block.AIR)
        //             }
        //         }
        //     }
        //     val iter = blocks.iterator()
        //     for (sx in x - 1..x + 1) for (sy in y..y + 2) for (sz in z..126) {
        //         event.player.instance.setBlock(sx, sy, sz, iter.next())
        //     }
        // }
    }
}
