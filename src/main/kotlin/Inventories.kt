package dev.bedcrab.hyperstom

import dev.bedcrab.hyperstom.code.CodeBlock
import dev.bedcrab.hyperstom.code.getCodeBlockMaterial
import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack

private val LOGGER = KotlinLogging.logger {}

abstract class HSInventory(type: InventoryType, private val name: String) {
    val inv = Inventory(type, name)
    protected abstract fun init()
    operator fun invoke() {
        init()
        LOGGER.info { "Registered $name inventory." }
    }
}

fun initInventories() {
    CodeBlocksInv()
}

object CodeBlocksInv : HSInventory(InventoryType.CHEST_3_ROW, "Code Blocks") {
    override fun init() {
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
}
