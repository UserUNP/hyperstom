package dev.bedcrab.hyperstom

import dev.bedcrab.hyperstom.code.*
import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.text.Component
import net.minestom.server.instance.block.Block
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

private val LOGGER = KotlinLogging.logger {}

fun initData() {
    initCodeBlocks()
    initCodeValueTypes()
    initCodeTargets()
    initEventValues()
    initEvents()
}

private lateinit var msBlockToCodeBlock: Map<Block, CodeBlock>
private lateinit var valTypeToItemStack: Map<CodeValueType<*>, ItemStack>
private lateinit var hsEventToItemStack: Map<HSEvent, ItemStack>
private lateinit var eventTargetToComponent: Map<EventTarget<*>, Component>
private lateinit var eventValToComponent: Map<EventValue<*>, Component>

fun getCodeBlock(msBlock: Block) = msBlockToCodeBlock[msBlock] ?: throw RuntimeException("${msBlock.name()} is not a code block!")
fun getMSBlock(hsBlock: CodeBlock) = msBlockToCodeBlock.keys.find { getCodeBlock(it) == hsBlock } ?: throw RuntimeException("Couldn't find Minestom block for code block $hsBlock")
fun getCodeBlockMaterial(hsBlock: CodeBlock) = Material.fromNamespaceId(getMSBlock(hsBlock).namespace()) ?: throw RuntimeException("What!")
fun getCodeValueItem(type: CodeValueType<*>) = valTypeToItemStack[type]
fun getHSEventItem(event: HSEvent) = hsEventToItemStack[event] ?: throw RuntimeException("What!")

fun initCodeBlocks() {
    msBlockToCodeBlock = mapOf(
        Block.LAPIS_BLOCK to CodeBlock.FUNCTION,
        Block.EMERALD_BLOCK to CodeBlock.PROCESS,

        Block.REDSTONE_BLOCK to CodeBlock.WORLD_EVENT,
        Block.DIAMOND_BLOCK to CodeBlock.PLAYER_EVENT,
        Block.GOLD_BLOCK to CodeBlock.NPC_EVENT,
        Block.COPPER_BLOCK to CodeBlock.DEV_EVENT,

        Block.NETHERRACK to CodeBlock.WORLD_ACTION,
        Block.COBBLESTONE to CodeBlock.PLAYER_ACTION,
        Block.MOSSY_COBBLESTONE to CodeBlock.NPC_ACTION,
        Block.RAW_IRON_BLOCK to CodeBlock.VAR_ACTION,
        Block.DEAD_BUBBLE_CORAL_BLOCK to CodeBlock.CONTROL,

        Block.NETHER_BRICKS to CodeBlock.IF_WORLD,
        Block.OAK_PLANKS to CodeBlock.IF_PLAYER,
        Block.BRICKS to CodeBlock.IF_NPC,
        Block.OBSIDIAN to CodeBlock.IF_VAR,

        Block.TARGET to CodeBlock.TARGET,
        Block.PRISMARINE_BRICKS to CodeBlock.REPEAT,
    )
    LOGGER.info { "Registered ${msBlockToCodeBlock.size} code blocks." }
}

fun initCodeValueTypes() {
    valTypeToItemStack = mapOf(
        NULL_VALUE_TYPE to ItemStack.of(Material.STRUCTURE_VOID).withDisplayName(MM.deserialize("<gradient:#0bb0a0:#0bb0fa>Null")),
        TYPE_VALUE_TYPE to ItemStack.of(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE).withDisplayName(MM.deserialize("<gradient:#9f9cff:#5fccfc>Type")),
        PARAM_VALUE_TYPE to ItemStack.of(Material.ENDER_EYE).withDisplayName(MM.deserialize("<gradient:#0090a0:#aaff7a:#a0f7a0>Parameter<#5fccfc>\\<>")),
        VAR_VALUE_TYPE to ItemStack.of(Material.MAGMA_CREAM).withDisplayName(MM.deserialize("<gradient:#709f0f:#e3f000:#fe9e00>Variable")),
        CONST_VALUE_TYPE to ItemStack.of(Material.FIRE_CHARGE).withDisplayName(MM.deserialize("<gradient:#7f5f5f:#e4660c:red>Constant<#5fccfc>\\<>")),
        EVENT_VALUE_TYPE to ItemStack.of(Material.NAME_TAG).withDisplayName(MM.deserialize("<gradient:#ffd070:#ffffaf:#ffd070>Event Value")),
        TARGET_VALUE_TYPE to ItemStack.of(Material.BOW).withDisplayName(MM.deserialize("Target")), //TODO: fancy colors
        FUNC_VALUE_TYPE to ItemStack.of(Material.FIREWORK_STAR).withDisplayName(MM.deserialize("<gradient:#0adadf:#0f7fff:#0acacf:#0f7fff>Function ref")),

        STR_VALUE_TYPE to ItemStack.of(Material.STRING).withDisplayName(MM.deserialize("<red>Str")),
        INT_VALUE_TYPE to ItemStack.of(Material.SLIME_BALL).withDisplayName(MM.deserialize("<red>Int")),
        FLOAT_VALUE_TYPE to ItemStack.of(Material.CLAY_BALL).withDisplayName(MM.deserialize("<red>Float")),
        BOOL_VALUE_TYPE to ItemStack.of(Material.COMPARATOR).withDisplayName(MM.deserialize("<red>Bool")),
        LIST_VALUE_TYPE to ItemStack.of(Material.GLOBE_BANNER_PATTERN).withDisplayName(MM.deserialize("<red>List<#5fccfc>\\<>")),
        EXPR_VALUE_TYPE to ItemStack.of(Material.GLOBE_BANNER_PATTERN).withDisplayName(MM.deserialize("<red>Expr<#5fccfc>\\<>")),

        ITEM_VALUE_TYPE to ItemStack.of(Material.ITEM_FRAME).withDisplayName(MM.deserialize("<gold>Item")),
        TEXT_VALUE_TYPE to ItemStack.of(Material.WRITABLE_BOOK).withDisplayName(MM.deserialize("<green>Txt")),
        PARTICLE_VALUE_TYPE to ItemStack.of(Material.WHITE_DYE).withDisplayName(MM.deserialize("<purple>Particle")),
    )
    LOGGER.info { "Registered ${valTypeToItemStack.size} code value types." }
}

fun initEvents() {
    hsEventToItemStack = mapOf(
        WORLD_INIT_EVENT to ItemStack.of(Material.REDSTONE_TORCH).withDisplayName(MM.deserialize("World Initialization")),
    )
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

enum class HSInventory(type: InventoryType, label: String) {
    CODE_BLOCKS(InventoryType.CHEST_3_ROW, "Code Blocks") {
        init {
            inv.setItemStack(0, ItemStack.of(getCodeBlockMaterial(CodeBlock.WORLD_EVENT)))
            inv.setItemStack(1, ItemStack.of(getCodeBlockMaterial(CodeBlock.PLAYER_EVENT)))
            inv.setItemStack(2, ItemStack.of(getCodeBlockMaterial(CodeBlock.NPC_EVENT)))
            inv.setItemStack(3, ItemStack.of(getCodeBlockMaterial(CodeBlock.DEV_EVENT)))
            inv.setItemStack(9, ItemStack.of(getCodeBlockMaterial(CodeBlock.WORLD_ACTION)))
            inv.setItemStack(10, ItemStack.of(getCodeBlockMaterial(CodeBlock.PLAYER_ACTION)))
            inv.setItemStack(11, ItemStack.of(getCodeBlockMaterial(CodeBlock.NPC_ACTION)))
            inv.setItemStack(12, ItemStack.of(getCodeBlockMaterial(CodeBlock.VAR_ACTION)))
            inv.setItemStack(18, ItemStack.of(getCodeBlockMaterial(CodeBlock.IF_WORLD)))
            inv.setItemStack(19, ItemStack.of(getCodeBlockMaterial(CodeBlock.IF_PLAYER)))
            inv.setItemStack(20, ItemStack.of(getCodeBlockMaterial(CodeBlock.IF_NPC)))
            inv.setItemStack(21, ItemStack.of(getCodeBlockMaterial(CodeBlock.IF_VAR)))

            inv.setItemStack(7, ItemStack.of(getCodeBlockMaterial(CodeBlock.FUNCTION)))
            inv.setItemStack(8, ItemStack.of(getCodeBlockMaterial(CodeBlock.PROCESS)))
            inv.setItemStack(24, ItemStack.of(getCodeBlockMaterial(CodeBlock.CONTROL)))
            inv.setItemStack(25, ItemStack.of(getCodeBlockMaterial(CodeBlock.TARGET)))
            inv.setItemStack(26, ItemStack.of(getCodeBlockMaterial(CodeBlock.REPEAT)))

            inv.addInventoryCondition { _, _, _, inventoryConditionResult ->
                inventoryConditionResult.cursorItem = inventoryConditionResult.clickedItem
                println("clicked ${inventoryConditionResult.clickedItem}")
                // TODO: fix this shit not working
            }

        }
    },
    ;
    val inv = Inventory(type, label)
}
