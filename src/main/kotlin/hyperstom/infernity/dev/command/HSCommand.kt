package hyperstom.infernity.dev.command

import hyperstom.infernity.dev.tagstore.TagStore
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.entity.Player

@Suppress("LeakingThis")
abstract class HSCommand(name: String) : Command(name) {
    init {
        setDefaultExecutor { sender, _ -> sender.sendMessage("Invalid syntax!") }
    }

    open inner class Syntax(arguments: Array<out Argument<*>>, executor: CommandExecutor) {
        init {
            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax
                try {
                    val store = TagStore(sender)
                    executor.apply(sender, store, context)
                } catch (e: Exception) {
                    sender.sendMessage("ERROR: ${e.message}")
                }
            }, *arguments)
        }
    }

    fun interface CommandExecutor {
        fun apply(player: Player, store: TagStore, context: CommandContext)
    }
}
