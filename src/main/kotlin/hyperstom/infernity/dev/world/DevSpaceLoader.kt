package hyperstom.infernity.dev.world

import net.hollowcube.polar.PolarLoader
import net.hollowcube.polar.PolarWorld
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.tag.Tag
import java.util.LinkedList
import java.util.concurrent.CompletableFuture

class DevSpaceLoader(val world: PolarWorld) : PolarLoader(world) {
    val eventBlocks = LinkedList<LineBlock>()
    val dataBlocks = LinkedList<LineBlock>()
    override fun loadChunk(instance: Instance, chunkX: Int, chunkZ: Int): CompletableFuture<Chunk?> = super.loadChunk(instance, chunkX, chunkZ).thenApply { loadedChunk ->
        val chunk = world.chunkAt(chunkX, chunkZ) ?: return@thenApply loadedChunk
        for (blockEntity in chunk.blockEntities) {
            val nbt = blockEntity.data ?: continue
            if ((Tag.String("type").read(nbt) ?: continue) != "block") continue
            val tag = Tag.String("codeblock").read(nbt) ?: continue
            val sign = ActionCodeBlock.Sign.parse(instance.getBlock(blockEntity.x - 1, blockEntity.y, blockEntity.z)) ?: continue
            val props = ActionCodeBlock.get(Block.fromNamespaceId(blockEntity.id ?: continue) ?: continue) ?: continue
            when (tag) {
                "event" -> eventBlocks.add(props.lineBlock(LineBlock.Type.EVENT, props.dataRegistry.get(sign.l2)))
                "function" -> dataBlocks.add(props.lineBlock(LineBlock.Type.FUNCTION, props.dataRegistry.get(sign.l2)))
                "process" -> dataBlocks.add(props.lineBlock(LineBlock.Type.PROCESS, props.dataRegistry.get(sign.l2)))
            }
        }
        loadedChunk
    }
}
