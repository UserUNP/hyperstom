package ma.userunp.hyperstom.command

import io.github.oshai.kotlinlogging.KotlinLogging
import ma.userunp.hyperstom.HSException
import ma.userunp.hyperstom.WorldMode
import ma.userunp.hyperstom.playerStates
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player

private val LOGGER = KotlinLogging.logger {}

fun initCommands() {
    val cmdManager = MinecraftServer.getCommandManager()
    for (cmd in listOf(
        AboutCommand, WorldCommand,
        PlayCommand, BuildCommand, DevCommand,
    )) cmdManager.register(cmd)
    LOGGER.info { "Registered ${cmdManager.commands.size} commands." }
}

private fun defaultExecutor(sender: CommandSender) = sender.sendMessage("Invalid syntax!")

class HSCommandContext(val player: Player, val ctx: CommandContext)

abstract class HSCommand(name: String) : Command(name) {
    init {
        lazy { setDefaultExecutor { sender, _ -> defaultExecutor(sender) } }
    }

    open inner class Syntax(arguments: Array<out Argument<*>>, executor: HSCommandContext.() -> Unit) {
        constructor(executor: HSCommandContext.() -> Unit) : this(emptyArray(), executor)

        init {
            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax
                try { executor(HSCommandContext(sender, context)) } catch (e: Exception) { sender.sendMessage(HSException(e).msg) }
            }, *arguments)
        }
    }
}

object AboutCommand : Command("about") {
    init {
        setDefaultExecutor { sender, _ -> sender.sendMessage("https://github.com/ma.userunp.hyperstom") }
    }
}

object PlayCommand : HSCommand("play") {
    init {
        Syntax { playerStates[player.uuid]?.apply {
            mode = WorldMode.PLAY
            player.setGameMode(GameMode.SURVIVAL)
            player.inventory.clear()
            player.setInstance(world.play, world.files.info.spawnLoc)
        } }
    }
}

object BuildCommand : HSCommand("build") {
    init {
        Syntax { playerStates[player.uuid]?.apply {
            mode = WorldMode.BUILD
            player.inventory.clear()
            player.setInstance(world.play, world.files.info.spawnLoc)
        } }
    }
}

object DevCommand : HSCommand("dev") {
    init {
        Syntax {
            player.sendMessage("In-game coding in not implemented yet!")
        }
    }
}

object WorldCommand : HSCommand("world") {
    init {
        addSubcommand(WorldCreateCommand)
        addSubcommand(WorldInvokeCommand)
        addSubcommand(WorldLSLabelsCommand)
        addSubcommand(WorldSaveCommand)
    }
}
