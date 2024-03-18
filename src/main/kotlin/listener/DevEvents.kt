package dev.bedcrab.hyperstom.listener

import dev.bedcrab.hyperstom.CodeBlocksInv
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.event.trait.PlayerEvent
import net.minestom.server.item.Material

fun initDevEvents(parentNode: EventNode<in PlayerEvent>) {
    parentNode.addListener(PlayerUseItemEvent::class.java, ::onItemClick)
}

fun onItemClick(event: PlayerUseItemEvent) {
    if (event.itemStack.material() == Material.DIAMOND) event.player.openInventory(CodeBlocksInv.inv)
}
