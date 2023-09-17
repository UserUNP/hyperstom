package hyperstom.infernity.dev.code.block

import hyperstom.infernity.dev.code.event.CodeEvent
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Entity
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.tag.Tag

data class EventBlock(val props: CodeBlock.Properties, val event: CodeEvent) : CodeBlock {
    override val defaultSign = CodeBlock.Sign(props.displayName, if (event.isDefault) "" else event.javaClass.simpleName, "", "")

    override fun place(point: Point, instance: Instance, signBlock: Block) {
        val x = point.blockX(); val y = point.blockY(); val z = point.blockZ()
        val block = props.block.withTag(Tag.String("type"), "block").withTag(Tag.String("codeblock"), "event")
        instance.setBlock(x, y, z + 1, Block.PISTON.withProperty("facing", "south"))
        instance.setBlock(x, y, z + 3, Block.PISTON.withProperty("facing", "north"))
        instance.setBlock(x - 1, y, z, signBlock)
        instance.setBlock(x, y, z, block)
    }

    override fun interpret(self: Entity, instance: Instance, sign: CodeBlock.Sign) {
    }
}
