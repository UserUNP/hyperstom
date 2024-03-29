package dev.bedcrab.hyperstom.code

import io.github.oshai.kotlinlogging.KotlinLogging
import dev.bedcrab.hyperstom.cborSerialize
import dev.bedcrab.hyperstom.getCodeBlock
import dev.bedcrab.hyperstom.shiftPoint
import kotlinx.serialization.Serializable
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block

private val LOGGER = KotlinLogging.logger {}

fun getCodeBlockType(type: String) = nameToCodeBlockType[type] ?: throw RuntimeException("Unsupported code block type! $type")
private val nameToCodeBlockType = mutableMapOf<String, CodeBlockType<*>>()
val DATA_BLOCK_TYPE = CodeBlockType("DATA", true) { TODO("Data code block types aren't implemented yet!") }
val EVENT_BLOCK_TYPE = CodeBlockType("EVENT", true) { getEvent(it) }
val ACTION_BLOCK_TYPE = CodeBlockType<Nothing>("ACTION", false)
val SCOPED_BLOCK_TYPE = CodeBlockType<Nothing>("SCOPED", true)
data class CodeBlockType<T : Invokable>(val name: String, val brackets: Boolean, private val getter: ((data: String) -> T)? = null) {
    val root = getter != null
    val space = if(brackets) 3 else 1 // the occupied space after the main block
    operator fun invoke(data: String) = getter?.let { it(data) } ?: throw RuntimeException("This code block is not of a root type!")
    init { nameToCodeBlockType[name] = this }
}

enum class CodeBlock(val type: CodeBlockType<*>) {
    FUNCTION(DATA_BLOCK_TYPE),
    PROCESS(DATA_BLOCK_TYPE),

    WORLD_EVENT(EVENT_BLOCK_TYPE),
    PLAYER_EVENT(EVENT_BLOCK_TYPE),
    NPC_EVENT(EVENT_BLOCK_TYPE),
    DEV_EVENT(EVENT_BLOCK_TYPE),

    WORLD_ACTION(ACTION_BLOCK_TYPE),
    PLAYER_ACTION(ACTION_BLOCK_TYPE),
    NPC_ACTION(ACTION_BLOCK_TYPE),
    VAR_ACTION(ACTION_BLOCK_TYPE),
    CONTROL(ACTION_BLOCK_TYPE),

    IF_WORLD(SCOPED_BLOCK_TYPE),
    IF_PLAYER(SCOPED_BLOCK_TYPE),
    IF_NPC(SCOPED_BLOCK_TYPE),
    IF_VAR(SCOPED_BLOCK_TYPE),

    TARGET(SCOPED_BLOCK_TYPE),
    REPEAT(SCOPED_BLOCK_TYPE),
    ;
    val label = name.replace("_", " ")
    init {
        LOGGER.info { "Registered code block $name with type ${type.name}@${type.hashCode()}" }
    }
}

fun <T : Invokable> rootCodeBlockEntry(type: CodeBlockType<T>, data: T) = RootCodeBlockEntry(type.name, data.toString())
@Serializable data class RootCodeBlockEntry(val type: String, val data: String)

val CONNECTOR_MS_BLOCK: Block = Block.STONE
val ARGS_CONTAINER_MS_BLOCK: Block = Block.BARREL
val INST_VIS_MS_BLOCK = Block.OAK_WALL_SIGN.withProperty("facing", "west")
val OPEN_BRACKET_MS_BLOCK = Block.PISTON.withProperty("facing", "south")
val CLOSE_BRACKET_MS_BLOCK = Block.PISTON.withProperty("facing", "north")

private val MS_UP_VEC = Vec(0.0, 1.0, 0.0)
private val MS_LEFT_VEC = Vec(-1.0, 0.0, 0.0)
private val MS_RIGHT_VEC = Vec(-1.0, 0.0, 0.0)
private val MS_FORWARD_VEC = Vec(0.0, 0.0, 1.0)
private val MS_BACKWARD_VEC = Vec(0.0, 0.0, -1.0)

fun getContainerPos(origin: Point) = shiftPoint(origin, MS_UP_VEC)
fun getConnectorPos(origin: Point) = shiftPoint(origin, MS_FORWARD_VEC)
fun getConnectorEndPos(origin: Point, type: CodeBlockType<*>) = shiftPoint(origin, MS_FORWARD_VEC, type.space.toDouble())
fun getInstVisPos(origin: Point) = shiftPoint(origin, MS_LEFT_VEC)

fun Instance.placeCodeBlock(msBlock: Block, hsBlock: CodeBlock, inst: InstProperties?, pos: Point) {
    val instVis = MinestomInstVisual(hsBlock.label, inst?.label ?: "", "", "")
    val instVisBlock = instVis().apply { withNbt(cborSerialize(instVis, nbt())) }
    setBlock(pos, msBlock)
    setBlock(getInstVisPos(pos), instVisBlock)
    setBlock(getContainerPos(pos), ARGS_CONTAINER_MS_BLOCK)
    if (hsBlock.type == ACTION_BLOCK_TYPE) setBlock(getConnectorPos(pos), CONNECTOR_MS_BLOCK)
    else {
        setBlock(getConnectorPos(pos), OPEN_BRACKET_MS_BLOCK)
        setBlock(getConnectorEndPos(pos, hsBlock.type), CLOSE_BRACKET_MS_BLOCK)
    }
}

fun findEndBracket(start: Point, getter: Block.Getter): Point? {
    var nestedCounter = 0
    var unchecked = 0
    var current = start
    var msBlock: Block
    do {
        current = shiftPoint(current, MS_FORWARD_VEC)
        msBlock = getter.getBlock(current)
        if (msBlock == Block.AIR) {
            unchecked++
            continue
        }
        unchecked = 0
        if (msBlock == CONNECTOR_MS_BLOCK) continue
        else if (msBlock == OPEN_BRACKET_MS_BLOCK) nestedCounter++
        else if (msBlock == CLOSE_BRACKET_MS_BLOCK) nestedCounter--
        else {
            val hsBlock = try { getCodeBlock(msBlock) } catch (_: Exception) { return null }
            if (hsBlock.type.root) return null
        }
    } while (nestedCounter != -1 && unchecked <= 4)
    if (msBlock != CLOSE_BRACKET_MS_BLOCK) return null
    return current
}

fun findRootCodeBlock(start: Point, getter: Block.Getter): Pair<Point?, CodeBlock?> {
    var current = start
    var unchecked = 0
    var hsBlock: CodeBlock? = null
    do {
        current = shiftPoint(current, MS_BACKWARD_VEC)
        val msBlock = getter.getBlock(current)
        if (msBlock == Block.AIR) {
            unchecked++
            continue
        }
        unchecked = 0
        hsBlock = try { getCodeBlock(msBlock) } catch (_: Exception) { continue }
        if (hsBlock!!.type.root) break
    } while (unchecked <= 4)
    if (hsBlock == null || !hsBlock.type.root) return null to null
    return current to hsBlock
}

fun checkBlockPlacement(getter: Block.Getter, pos: Point) {
    if (getter.getBlock(shiftPoint(pos, MS_LEFT_VEC)) != Block.AIR) throw RuntimeException("Invalid block placement!")
    if (getter.getBlock(shiftPoint(pos, MS_RIGHT_VEC)) != Block.AIR) throw RuntimeException("Invalid block placement!")
}

