package hyperstom.infernity.dev.code.block

import hyperstom.infernity.dev.Utils
import hyperstom.infernity.dev.code.interpreter.LocalThreadContext
import hyperstom.infernity.dev.code.interpreter.actionCtxResolver
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.tag.Tag
import net.minestom.server.utils.Direction

abstract class ExecutableCodeBlock(private val props: Properties) : CodeBlock<LocalThreadContext> {
    override fun place(point: Point, instance: Instance, signBlock: Block) {
        val block = props.block.withTag(Tag.String("type"), "block").withTag(Tag.String("codeblock"), props.type.name)
        instance.setBlock(Utils.shiftPoint(point, Direction.SOUTH), Block.STONE)
        instance.setBlock(Utils.shiftPoint(point, Direction.UP), Block.BARREL.withTag(Tag.String("type"), "container"))
        instance.setBlock(Utils.shiftPoint(point, Direction.WEST), signBlock)
        instance.setBlock(point, block)
    }

    override fun interpret(sign: CodeBlock.Sign, ctx: LocalThreadContext) = execute(props.contextResolver.resolve(sign, ctx))
    abstract fun execute(context: LocalThreadContext)

    enum class Type { ACTION, SCOPED }
    enum class Properties(val type: Type, override val block: Block) : CodeBlock.Properties<ExecutableCodeBlock> {
        PLAYER_ACTION(Type.ACTION, Block.COBBLESTONE)
        ;
        
        override lateinit var registry: CodeBlock.Registry<ExecutableCodeBlock>
        val contextResolver = when(type) {
            Type.ACTION -> actionCtxResolver
            Type.SCOPED -> TODO("scopedCtxResolver")
        }
    }
}
