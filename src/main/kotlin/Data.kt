package userunp.hyperstom

import userunp.hyperstom.code.*
import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.text.Component

private val LOGGER = KotlinLogging.logger {}

fun initData() {
    initCodeBlocks()
    initCodeValueTypes()
    initCodeTargets()
    initEventValues()
    initEvents()
}

private lateinit var codeBlockToComponent: Map<CodeBlock, Component>
private lateinit var codeValTypeToComponent: Map<CodeValType<*>, Component>
private lateinit var hsEventToComponent: Map<HSEvent<*>, Component>
private lateinit var eventTargetToComponent: Map<EventTarget<*>, Component>
private lateinit var eventValToComponent: Map<EventVal<*, *>, Component>

fun initCodeBlocks() {
    codeBlockToComponent = mapOf(
        CodeBlock.FUNCTION to MM.deserialize("Function"),
        CodeBlock.PROCESS to MM.deserialize("Process"),

        CodeBlock.WORLD_EVENT to MM.deserialize("World Event"),
        CodeBlock.PLAYER_EVENT to MM.deserialize("Player Event"),
        CodeBlock.NPC_EVENT to MM.deserialize("NPC Event"),
        CodeBlock.DEV_EVENT to MM.deserialize("Dev Event"),

        CodeBlock.WORLD_ACTION to MM.deserialize("World Action"),
        CodeBlock.PLAYER_ACTION to MM.deserialize("Player Action"),
        CodeBlock.NPC_ACTION to MM.deserialize("Npc Action"),
        CodeBlock.VAR_ACTION to MM.deserialize("Var Action"),
        CodeBlock.CONTROL to MM.deserialize("Control"),

        CodeBlock.IF_WORLD to MM.deserialize("If World"),
        CodeBlock.IF_PLAYER to MM.deserialize("If Player"),
        CodeBlock.IF_NPC to MM.deserialize("If NPC"),
        CodeBlock.IF_VAR to MM.deserialize("If Var"),

        CodeBlock.TARGET to MM.deserialize("Target"),
        CodeBlock.REPEAT to MM.deserialize("Repeat"),
    )
    LOGGER.info { "Registered ${codeBlockToComponent.size} code blocks." }
}

fun initCodeValueTypes() {
    codeValTypeToComponent = mapOf(
        VAL_TYPE_NULL to MM.deserialize("<gradient:#0bb0a0:#0bb0fa>Null"),
        VAL_TYPE_TYPE to MM.deserialize("<gradient:#9f9cff:#5fccfc>Type"),
        VAL_TYPE_PARAM to MM.deserialize("<gradient:#0090a0:#aaff7a:#a0f7a0>Parameter<#5fccfc>\\<>"),
        VAL_TYPE_VAR to MM.deserialize("<gradient:#709f0f:#e3f000:#fe9e00>Variable"),
        VAL_TYPE_GLOBAL to MM.deserialize("<gradient:#7f5f5f:#e4660c:red>Global<#5fccfc>\\<>"),
        VAL_TYPE_EVENT_VAL to MM.deserialize("<gradient:#ffd070:#ffffaf:#ffd070>Event Value"),
        VAL_TYPE_TARGET to MM.deserialize("Target"), //TODO: fancy colos
        VAL_TYPE_FUNC to MM.deserialize("<gradient:#0adadf:#0f7fff:#0acacf:#0f7fff>Function ref"),

        VAL_TYPE_STR to MM.deserialize("<red>Str"),
        VAL_TYPE_NUM to MM.deserialize("<red>Num"),
        VAL_TYPE_BOOL to MM.deserialize("<red>Bool"),
        VAL_TYPE_LIST to MM.deserialize("<red>List<#5fccfc>\\<>"),
        VAL_TYPE_EXPR to MM.deserialize("<red>Expr<#5fccfc>\\<>"),

        VAL_TYPE_ITEM to MM.deserialize("<gold>Item"),
        VAL_TYPE_TXT to MM.deserialize("<green>Txt"),
        VAL_TYPE_PARTICLE to MM.deserialize("<purple>Particle"),
    )
    LOGGER.info { "Registered ${codeValTypeToComponent.size} code value types." }
}

fun initCodeTargets() {
    eventTargetToComponent = mapOf(
        TARGET_PLAYERS_ALL to MM.deserialize("All Players"),
        TARGET_PLAYER_RAND to MM.deserialize("Random Player"),
        TARGET_NPC_ALL to MM.deserialize("All NPCs"),
        TARGET_NPC_RAND to MM.deserialize("Random NPC"),
        TARGET_DEFAULT to MM.deserialize("Default"),

        TARGET_ENTITY_CLICKED to MM.deserialize("Clicked Entity"),
    )
    LOGGER.info { "Registered ${eventTargetToComponent.size} event targets." }
}

fun initEventValues() {
    eventValToComponent = mapOf(
        EVENT_VAL_WORLD_TITLE to MM.deserialize("World Name"),
        EVENT_VAL_ENTITY_UUID to MM.deserialize("Default Entity UUID")
    )
    LOGGER.info { "Registered ${eventValToComponent.size} event values." }
}

fun initEvents() {
    hsEventToComponent = mapOf(
        EVENT_WORLD_INIT to MM.deserialize("World Initialization"),
        EVENT_PLAYER_CHAT to MM.deserialize("Player Use Chat")
    )
    LOGGER.info { "Registered ${hsEventToComponent.size} events." }
}
