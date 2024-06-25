package userunp.hyperstom.code

enum class CodeBlockType(val brackets: Boolean, private val getter: ((data: String) -> Invokable)? = null) {
    DATA(true, { HSProcess(it) }), // only case for invoking a data code block is for a process
    EVENT(true, { getEvent(it) }),
    ACTION(false), SCOPED(true),
    ;

    fun get(data: String) = getter?.let { it(data) }
        ?: throw RuntimeException("This code block is not of a root type!")
}

enum class CodeBlock(val type: CodeBlockType) {
    FUNCTION(CodeBlockType.DATA),
    PROCESS(CodeBlockType.DATA),

    WORLD_EVENT(CodeBlockType.EVENT),
    PLAYER_EVENT(CodeBlockType.EVENT),
    NPC_EVENT(CodeBlockType.EVENT),
    DEV_EVENT(CodeBlockType.EVENT),

    WORLD_ACTION(CodeBlockType.ACTION),
    PLAYER_ACTION(CodeBlockType.ACTION),
    NPC_ACTION(CodeBlockType.ACTION),
    VAR_ACTION(CodeBlockType.ACTION),
    CONTROL(CodeBlockType.SCOPED),

    IF_WORLD(CodeBlockType.SCOPED),
    IF_PLAYER(CodeBlockType.SCOPED),
    IF_NPC(CodeBlockType.SCOPED),
    IF_VAR(CodeBlockType.SCOPED),

    TARGET(CodeBlockType.SCOPED),
    REPEAT(CodeBlockType.SCOPED),
    ;
}
