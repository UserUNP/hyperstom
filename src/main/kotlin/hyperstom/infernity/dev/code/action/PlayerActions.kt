package hyperstom.infernity.dev.code.action

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance

private object DefaultAction : CodeAction {
    override val description: List<TextComponent> = listOf()
    override fun execute(instance: Instance, entity: Entity) {}
}

private enum class PlayerCodeAction : CodeAction {
    SendMessage {
        override val description: List<TextComponent> = listOf(
            Component.text("Send a message to the selected target."),
            Component.text("Example: SendMessage(\"Hello World!\")")
        )
        override fun execute(instance: Instance, entity: Entity) = if (entity is Player) entity.sendMessage("message sent") else {}
    },
}

object PlayerActions : CodeAction.Registry() {
    override val defaultAction: CodeAction = DefaultAction
    override fun get(index: Int): CodeAction = PlayerCodeAction.entries[index]
}
