package hyperstom.infernity.dev.code.block

import hyperstom.infernity.dev.code.interpreter.InstructionContext
import hyperstom.infernity.dev.gsonSerializer
import hyperstom.infernity.dev.plainSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.tag.Tag
import org.jglrxavpok.hephaistos.nbt.*

interface CodeBlock<T : InstructionContext> {
    fun place(point: Point, instance: Instance, signBlock: Block)
    fun interpret(sign: Sign, ctx: T)

    interface Properties<T> {
        val block: Block
        var registry: Registry<T>
    }

    interface Registry<T> {
        val default: T
        fun get(index: Int): T
    }

    data class ContextResolver<T : InstructionContext, R : InstructionContext>(val resolve: (sign: Sign, ctx: T) -> R)

    data class InstructionResolver<T>(val resolve: (sign: Sign) -> T)

    data class Sign(val l1: String, val l2: String, val l3: String, val l4: String) {
        val block = Block.OAK_WALL_SIGN
            .withProperty("facing", "west")
            .withTag(Tag.NBT("front_text"), NBT.Compound(mapOf(
                "has_glowing_text" to NBT.FALSE,
                "color" to NBT.String("black"),
                "messages" to NBT.List(NBTType.TAG_String, listOf(
                    NBT.String(gsonSerializer.serialize(Component.text(l1).decorate(TextDecoration.BOLD))),
                    NBT.String(gsonSerializer.serialize(Component.text(l2))),
                    NBT.String(gsonSerializer.serialize(Component.text(l3))),
                    NBT.String(gsonSerializer.serialize(Component.text(l4))),
                ))
            )))

        companion object {
            fun parse(block: Block): Sign? {
                if (block != Block.OAK_WALL_SIGN) return null
                val nbt = Tag.NBT("messages").read(block.getTag(Tag.NBT("front_text")) as NBTCompoundLike? ?: return null) as NBTList<*>? ?: return null
                if (nbt.subtagType == NBTType.TAG_String) return null
                return Sign(
                    plainSerializer.serialize(gsonSerializer.deserialize((nbt[0] as NBTString).value)),
                    plainSerializer.serialize(gsonSerializer.deserialize((nbt[1] as NBTString).value)),
                    plainSerializer.serialize(gsonSerializer.deserialize((nbt[2] as NBTString).value)),
                    plainSerializer.serialize(gsonSerializer.deserialize((nbt[3] as NBTString).value)),
                )
            }
        }
    }
}
