package hyperstom.infernity.dev.code.block

import hyperstom.infernity.dev.code.action.CodeAction
import hyperstom.infernity.dev.code.event.CodeEvent
import hyperstom.infernity.dev.gsonSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Entity
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.tag.Tag
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.NBTType

interface CodeBlock {
    val defaultSign: Sign
    fun place(point: Point, instance: Instance, signBlock: Block)
    fun interpret(self: Entity, instance: Instance, sign: Sign)

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
    }

    enum class Type { EVENT, ACTION, SCOPED, DATA }

    enum class Properties(val block: Block, private val type: Type) {
        PLAYER_EVENT(Block.DIAMOND_BLOCK, Type.EVENT),
        PLAYER_ACTION(Block.COBBLESTONE, Type.ACTION),
        IF_PLAYER(Block.OAK_PLANKS, Type.SCOPED),
        FUNCTION(Block.LAPIS_BLOCK, Type.DATA),
        ;

        val displayName = name.replace('_', ' ')
        private lateinit var actionsRegistry: CodeAction.Registry
        private lateinit var eventsRegistry: CodeEvent.Registry
        fun setRegistry(registry: CodeAction.Registry): Properties {
            actionsRegistry = registry
            return this
        }
        fun setRegistry(registry: CodeEvent.Registry): Properties {
            eventsRegistry = registry
            return this
        }

        private fun isEventBlock() = type == Type.EVENT
        private fun isActionBlock() = type == Type.ACTION
        private fun isScopedBlock() = type == Type.SCOPED
        private fun isDataBlock() = type == Type.DATA
        fun default() = when(type) {
            Type.EVENT -> eventBlock("")
            Type.ACTION -> actionBlock(actionsRegistry.defaultAction)
            Type.SCOPED -> scopedBlock(actionsRegistry.defaultAction)
            Type.DATA -> dataBlock(actionsRegistry.defaultAction)
        }
        fun eventBlock(event: String) = if (isEventBlock()) EventBlock(this, event) else throw RuntimeException("$name is not an event block!")
        fun actionBlock(action: CodeAction) = if (isActionBlock()) ActionBlock(this, action) else throw RuntimeException("$name is not an action block!")
        fun scopedBlock(action: CodeAction) = if (isScopedBlock()) ScopedBlock(this, action) else throw RuntimeException("$name is not a scoped block!")
        fun dataBlock(name: CodeAction) = if (isDataBlock()) ScopedBlock(this, name) else throw RuntimeException("$name is not a data block!")
    }

    companion object {
        fun get(block: Block): Properties? {
            for (b in Properties.entries) if (b.block == block) return b
            return null
        }
    }
}
