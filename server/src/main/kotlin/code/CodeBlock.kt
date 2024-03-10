package dev.bedcrab.hyperstom.code

import dev.bedcrab.hyperstom.*
import kotlinx.serialization.Serializable
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block

val CONNECTOR_MS_BLOCK: Block = Block.STONE
val ARGS_CONTAINER_MS_BLOCK: Block = Block.BARREL
val INST_VIS_MS_BLOCK = Block.OAK_WALL_SIGN.withProperty("facing", "west")
val OPEN_BRACKET_MS_BLOCK = Block.PISTON.withProperty("facing", "south")
val CLOSE_BRACKET_MS_BLOCK = Block.PISTON.withProperty("facing", "north")
fun isOpenBracket(block: Block) = block == OPEN_BRACKET_MS_BLOCK
fun isCloseBracket(block: Block) = block == CLOSE_BRACKET_MS_BLOCK

val DATA_TYPE = CodeBlock.Type("DATA", true) { TODO("Data code block types aren't implemented yet!") }
val EVENT_TYPE = CodeBlock.Type("EVENT", true) { HSEvent.valueOf(it) }
val ACTION_TYPE = CodeBlock.Type<Nothing>("ACTION", false)
val SCOPED_TYPE = CodeBlock.Type<Nothing>("SCOPED", true)
fun <T : Invokable> getTypeEntry(type: CodeBlock.Type<T>, data: T) = CodeBlock.TypeEntry(type.name, data.toString())

enum class CodeBlock(val msBlock: Block, val type: Type<*>) {
    FUNCTION(Block.LAPIS_BLOCK, DATA_TYPE),
    PROCESS(Block.EMERALD_BLOCK, DATA_TYPE),

    WORLD_EVENT(Block.REDSTONE_BLOCK, EVENT_TYPE),
    PLAYER_EVENT(Block.DIAMOND_BLOCK, EVENT_TYPE),
    NPC_EVENT(Block.GOLD_BLOCK, EVENT_TYPE),

    WORLD_ACTION(Block.NETHERRACK, ACTION_TYPE),
    PLAYER_ACTION(Block.COBBLESTONE, ACTION_TYPE),
    NPC_ACTION(Block.MOSSY_COBBLESTONE, ACTION_TYPE),
    SET_VAR(Block.RAW_IRON_BLOCK, ACTION_TYPE),

    IF_WORLD(Block.NETHER_BRICKS, SCOPED_TYPE),
    IF_PLAYER(Block.OAK_PLANKS, SCOPED_TYPE),
    IF_NPC(Block.BRICKS, SCOPED_TYPE),
    IF_VAR(Block.OBSIDIAN, SCOPED_TYPE),

    TARGET(Block.TARGET, SCOPED_TYPE),
    REPEAT(Block.PRISMARINE_BRICKS, SCOPED_TYPE),
    ;

    private val label = name.replace("_", " ")
    fun place(devSpace: Instance, pos: Point, inst: InstProperties?) {
        val instVis = Instruction.Visual(label, inst?.label ?: "", "", "")
        val instVisBlock = instVis.block.withNbt(cborSerialize(instVis, instVis.block.nbt()))
        devSpace.setBlock(pos, msBlock)
        devSpace.setBlock(shiftPoint(pos, LEFT_VEC), instVisBlock)
        devSpace.setBlock(shiftPoint(pos, UP_VEC), ARGS_CONTAINER_MS_BLOCK)
        if (type == ACTION_TYPE) devSpace.setBlock(shiftPoint(pos, FORWARD_VEC), CONNECTOR_MS_BLOCK)
        else {
            devSpace.setBlock(shiftPoint(pos, FORWARD_VEC), OPEN_BRACKET_MS_BLOCK)
            devSpace.setBlock(shiftPoint(pos, FORWARD_VEC, type.space.toDouble()), CLOSE_BRACKET_MS_BLOCK)
        }
    }

    @Serializable
    data class TypeEntry(val type: String, val data: String)
    class Type<T : Invokable>(val name: String, brackets: Boolean, private val getter: ((data: String) -> T)? = null) {
        val root = getter != null
        val space = if(brackets) 3 else 1
        operator fun invoke(data: String) = getter?.invoke(data) ?: throw RuntimeException("This code block is not of a root type!")
        init { map[name] = this }
        companion object {
            private val map: MutableMap<String, Type<*>> = mutableMapOf()
            fun from(type: String) = map[type] ?: throw RuntimeException("Unsupported code block type! $type")
        }
    }

    companion object {
        fun from(msBlock: Block): CodeBlock {
            for (hsBlock in entries) if (hsBlock.msBlock.name() == msBlock.name()) return hsBlock
            throw RuntimeException("${msBlock.name()} is not a code block!")
        }
    }
}
