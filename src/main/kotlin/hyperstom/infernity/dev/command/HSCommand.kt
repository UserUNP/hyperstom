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

    inner class Syntax(vararg arguments: Argument<*>, executor: CommandExecutor) {
        init {
            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax
                try { executor.apply(sender, TagStore(sender), context) } catch (e: Exception) {
                    sender.sendMessage("Error: "+e.message)
                }
            }, *arguments)
        }
    }

    fun interface CommandExecutor {
        fun apply(player: Player, store: TagStore, context: CommandContext)
    }
}
