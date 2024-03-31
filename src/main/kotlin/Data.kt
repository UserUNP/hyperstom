package dev.bedcrab.hyperstom

import dev.bedcrab.hyperstom.code.*
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
private lateinit var codeValueTypeToComponent: Map<CodeValueType<*>, Component>
private lateinit var hsEventToComponent: Map<HSEvent, Component>
private lateinit var eventTargetToComponent: Map<EventTarget<*>, Component>
private lateinit var eventValToComponent: Map<EventValue<*>, Component>

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
    codeValueTypeToComponent = mapOf(
        NULL_VALUE_TYPE to MM.deserialize("<gradient:#0bb0a0:#0bb0fa>Null"),
        TYPE_VALUE_TYPE to MM.deserialize("<gradient:#9f9cff:#5fccfc>Type"),
        PARAM_VALUE_TYPE to MM.deserialize("<gradient:#0090a0:#aaff7a:#a0f7a0>Parameter<#5fccfc>\\<>"),
        VAR_VALUE_TYPE to MM.deserialize("<gradient:#709f0f:#e3f000:#fe9e00>Variable"),
        CONST_VALUE_TYPE to MM.deserialize("<gradient:#7f5f5f:#e4660c:red>Constant<#5fccfc>\\<>"),
        EVENT_VALUE_TYPE to MM.deserialize("<gradient:#ffd070:#ffffaf:#ffd070>Event Value"),
        TARGET_VALUE_TYPE to MM.deserialize("Target"), //TODO: fancy colos
        FUNC_VALUE_TYPE to MM.deserialize("<gradient:#0adadf:#0f7fff:#0acacf:#0f7fff>Function ref"),

        STR_VALUE_TYPE to MM.deserialize("<red>Str"),
        INT_VALUE_TYPE to MM.deserialize("<red>Int"),
        FLOAT_VALUE_TYPE to MM.deserialize("<red>Float"),
        BOOL_VALUE_TYPE to MM.deserialize("<red>Bool"),
        LIST_VALUE_TYPE to MM.deserialize("<red>List<#5fccfc>\\<>"),
        EXPR_VALUE_TYPE to MM.deserialize("<red>Expr<#5fccfc>\\<>"),

        ITEM_VALUE_TYPE to MM.deserialize("<gold>Item"),
        TEXT_VALUE_TYPE to MM.deserialize("<green>Txt"),
        PARTICLE_VALUE_TYPE to MM.deserialize("<purple>Particle"),
    )
    LOGGER.info { "Registered ${codeValueTypeToComponent.size} code value types." }
}

fun initCodeTargets() {
    eventTargetToComponent = mapOf(
        PLAYERS_ALL_TARGET to MM.deserialize("All Players"),
        PLAYER_RAND_TARGET to MM.deserialize("Random Player"),
        NPC_ALL_TARGET to MM.deserialize("All NPCs"),
        NPC_RAND_TARGET to MM.deserialize("Random NPC"),
        DEFAULT_TARGET to MM.deserialize("Default"),

        ENTITY_CLICKED_TARGET to MM.deserialize("Clicked Entity"),
    )
    LOGGER.info { "Registered ${eventTargetToComponent.size} event targets." }
}

fun initEventValues() {
    eventValToComponent = mapOf(
        WORLD_NAME_EVENT_VAL to MM.deserialize("World Name"),
    )
    LOGGER.info { "Registered ${eventValToComponent.size} event values." }
}

fun initEvents() {
    hsEventToComponent = mapOf(
        WORLD_INIT_EVENT to MM.deserialize("World Initialization"),
    )
    LOGGER.info { "Registered ${hsEventToComponent.size} events." }
}
