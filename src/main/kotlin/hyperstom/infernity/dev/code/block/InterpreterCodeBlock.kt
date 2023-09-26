package hyperstom.infernity.dev.code.block

import hyperstom.infernity.dev.Utils
import hyperstom.infernity.dev.code.interpreter.InterpreterContext
import hyperstom.infernity.dev.event.HSEvent
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.tag.Tag
import net.minestom.server.utils.Direction

abstract class InterpreterCodeBlock<T : HSEvent>(private val props: Properties) : CodeBlock<InterpreterContext<T>> {
    override fun place(point: Point, instance: Instance, signBlock: Block) {
        val block = props.block.withTag(Tag.String("type"), "block").withTag(Tag.String("codeblock"), props.type.name)
        instance.setBlock(Utils.shiftPoint(point, Direction.SOUTH), Block.STICKY_PISTON.withProperty("facing", "south"))
        instance.setBlock(Utils.shiftPoint(point, Direction.SOUTH, 3.0), Block.STICKY_PISTON.withProperty("facing", "north"))
        if (props.type != Type.EVENT) instance.setBlock(Utils.shiftPoint(point, Direction.UP), Block.BARREL.withTag(Tag.String("type"), "container"))
        instance.setBlock(Utils.shiftPoint(point, Direction.WEST), signBlock)
        instance.setBlock(point, block)
    }

    override fun interpret(sign: CodeBlock.Sign, ctx: InterpreterContext<T>) = interpret(props.contextResolver.resolve(sign, ctx))
    abstract fun interpret(ctx: InterpreterContext<T>)

    enum class Type { EVENT, FUNCTION }
    enum class Properties(val type: Type, override val block: Block) : CodeBlock.Properties<InterpreterCodeBlock<*>> {
        WORLD_EVENT(Type.EVENT, Block.REDSTONE_BLOCK)
        ;

        override lateinit var registry: CodeBlock.Registry<InterpreterCodeBlock<*>>
        val contextResolver = when(type) {
            Type.EVENT -> TODO("eventCtxResolver")
            Type.FUNCTION -> TODO("functionCtxResolver")
        }
    }
}
