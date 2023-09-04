package hyperstom.infernity.dev.code.block

import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Entity
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.tag.Tag

data class EventBlock(val props: CodeBlock.Properties, val event: String) : CodeBlock {
    override val defaultSign = CodeBlock.Sign(props.displayName, event, "", "")

    override fun place(point: Point, instance: Instance, signBlock: Block) {
        val x = point.blockX(); val y = point.blockY(); val z = point.blockZ()
        instance.setBlock(x, y, z, props.block.withTag(Tag.String("type"), "block"))
        instance.setBlock(x, y, z + 1, Block.PISTON.withProperty("facing", "south"))
        instance.setBlock(x, y, z + 3, Block.PISTON.withProperty("facing", "north"))
        instance.setBlock(x - 1, y, z, signBlock)
    }

    override fun interpret(self: Entity, instance: Instance, sign: CodeBlock.Sign) {
    }
}
