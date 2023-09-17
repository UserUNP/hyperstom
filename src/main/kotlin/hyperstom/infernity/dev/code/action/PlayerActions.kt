package hyperstom.infernity.dev.code.action

import hyperstom.infernity.dev.code.interpreter.ExecutionContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.minestom.server.entity.Player

private object DefaultAction : CodeAction {
    override val isDefault = true
    override val description: List<TextComponent> = listOf()
    override fun execute(context: ExecutionContext) {}
}

private enum class PlayerCodeAction : CodeAction {
    SendMessage {
        override val isDefault = false
        override val description: List<TextComponent> = listOf(
            Component.text("Send a message to the selected target."),
        )
        override fun execute(context: ExecutionContext) = if (context.entity is Player) (context.entity as Player).sendMessage("message sent") else {}
    },
}

object PlayerActions : CodeAction.Registry() {
    override val default: CodeAction = DefaultAction
    override fun get(index: Int): CodeAction = PlayerCodeAction.entries[index]
}
