package hyperstom.infernity.dev.code.block

import hyperstom.infernity.dev.code.action.CodeAction
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Entity
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.tag.Tag
import net.minestom.server.utils.entity.EntityFinder

data class ActionBlock(val props: CodeBlock.Properties, val action: CodeAction) : CodeBlock {
    override val defaultSign = CodeBlock.Sign(props.displayName, action.javaClass.simpleName, "", "")

    override fun place(point: Point, instance: Instance, signBlock: Block) {
        val x = point.blockX(); val y = point.blockY(); val z = point.blockZ()
        instance.setBlock(x, y, z, props.block.withTag(Tag.String("type"), "block"))
        instance.setBlock(x, y, z + 1, Block.STONE)
        instance.setBlock(x, y + 1, z, argumentsContainer())
        instance.setBlock(x - 1, y, z, signBlock)
    }

    override fun interpret(self: Entity, instance: Instance, sign: CodeBlock.Sign) {
        val target = EntityFinder.TargetSelector.valueOf(sign.l4)
        val entities = EntityFinder().setTargetSelector(target).find(instance, self)
        for (entity in entities) action.execute(instance, entity)
    }

    private fun argumentsContainer() = Block.BARREL.withTag(Tag.String("type"), "container")
}
