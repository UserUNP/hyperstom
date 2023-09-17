package hyperstom.infernity.dev.world

import hyperstom.infernity.dev.code.block.CodeBlock
import hyperstom.infernity.dev.code.block.EventBlock
import net.hollowcube.polar.PolarLoader
import net.hollowcube.polar.PolarWorld
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.tag.Tag
import java.util.LinkedList
import java.util.concurrent.CompletableFuture

class DevSpaceLoader(val world: PolarWorld) : PolarLoader(world) {
    override fun loadChunk(instance: Instance, chunkX: Int, chunkZ: Int): CompletableFuture<Chunk?> = super.loadChunk(instance, chunkX, chunkZ).thenApply { loadedChunk ->
        val chunk = world.chunkAt(chunkX, chunkZ) ?: return@thenApply loadedChunk
        val eventBlocks = LinkedList<EventBlock>()
        for (blockEntity in chunk.blockEntities) {
            val nbt = blockEntity.data ?: continue
            if ((Tag.String("type").read(nbt) ?: continue) != "block") continue
            val tag = Tag.String("codeblock").read(nbt) ?: continue
            val sign =
                CodeBlock.Sign.parse(instance.getBlock(blockEntity.x - 1, blockEntity.y, blockEntity.z)) ?: continue
            if (tag == "event") {
                val props = CodeBlock.get(Block.fromNamespaceId(blockEntity.id ?: continue) ?: continue) ?: continue
                val block = props.eventBlock(props.eventsRegistry.get(sign.l2))
                eventBlocks.add(block)
            }
        }
        loadedChunk
    }
}
