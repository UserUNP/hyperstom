package ma.userunp.hyperstom.code

enum class CodeBlock(val type: CodeLabelType<*>? = null) {
    FUNCTION(LabelDataType), PROCESS(LabelDataType),

    WORLD_EVENT(LabelEventType), DEV_EVENT(LabelEventType),
    PLAYER_EVENT(LabelEventType), NPC_EVENT(LabelEventType),

    WORLD_ACTION, VAR_ACTION,
    PLAYER_ACTION, NPC_ACTION,
    CONTROL,

    IF_WORLD(LabelScopedType), IF_VAR(LabelScopedType),
    IF_PLAYER(LabelScopedType), IF_NPC(LabelScopedType),

    TARGET(LabelScopedType), REPEAT(LabelScopedType),
}
