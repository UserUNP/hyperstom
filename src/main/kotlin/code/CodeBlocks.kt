package dev.bedcrab.hyperstom.code

import kotlinx.serialization.Serializable

fun getCodeBlockType(type: String) = nameToCodeBlockType[type] ?: throw RuntimeException("Unsupported code block type! $type")
private val nameToCodeBlockType = mutableMapOf<String, CodeBlockType<*>>()
val DATA_BLOCK_TYPE = CodeBlockType("DATA", true) { TODO("Data code block types aren't implemented yet!") }
val EVENT_BLOCK_TYPE = CodeBlockType("EVENT", true) { getEvent(it) }
val ACTION_BLOCK_TYPE = CodeBlockType<Nothing>("ACTION", false)
val SCOPED_BLOCK_TYPE = CodeBlockType<Nothing>("SCOPED", true)
data class CodeBlockType<T : Invokable>(val name: String, val brackets: Boolean, private val getter: ((data: String) -> T)? = null) {
    val root = getter != null
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
}

fun <T : Invokable> rootCodeBlockEntry(type: CodeBlockType<T>, data: T) = RootCodeBlockEntry(type.name, data.toString())
@Serializable data class RootCodeBlockEntry(val type: String, val data: String)
