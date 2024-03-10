package dev.bedcrab.hyperstom.command

import dev.bedcrab.hyperstom.datastore.StorePlayerState
import dev.bedcrab.hyperstom.datastore.TagStore
import dev.bedcrab.hyperstom.world.ModeHandler
import dev.bedcrab.hyperstom.world.WorldManager
import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.command.CommandManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.entity.Player
import java.util.UUID

private val LOGGER = KotlinLogging.logger {}

fun initCommands(cmdManager: CommandManager) {
    for (cmd in listOf(
        AboutCommand(), WorldCommand(),
        PlayCommand(), BuildCommand(), DevCommand(),
    )) cmdManager.register(cmd)
    LOGGER.info { "Initialized ${cmdManager.commands.size} commands." }
}

private fun defaultExecutor(sender: CommandSender) = sender.sendMessage("Invalid syntax!")
private fun getWorld(id: UUID) = WorldManager.worlds[id] ?: throw NullPointerException("World with id $id does not exist!")

abstract class HSCommand(name: String) : Command(name) {
    init {
        lazy { setDefaultExecutor { sender, _ -> defaultExecutor(sender) } }
    }

    open inner class Syntax(arguments: Array<out Argument<*>>, executor: HSCommandContext.() -> Unit) {
        constructor(executor: HSCommandContext.() -> Unit) : this(emptyArray(), executor)

        init {
            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax
                try {
                    TagStore(sender).use { executor(HSCommandContext(sender, context)) }
                } catch (e: Exception) {
                    sender.sendMessage("ERROR: ${e.message}")
                }
            }, *arguments)
        }
    }
    data class HSCommandContext(val player: Player, val context: CommandContext)
}

class AboutCommand : Command("about") {
    init {
        setDefaultExecutor { sender, _ -> sender.sendMessage("https://github.com/Hyperstom/Hyperstom") }
    }
}

class PlayCommand : HSCommand("play") {
    init {
        Syntax {
            TagStore(player).use {
                val state = it.read(StorePlayerState::class)
                val world = getWorld(state.id)
                it.write(state.withMode(ModeHandler.Mode.PLAY))
                world.setInstanceToPlay(player)
            }
        }
    }
}

class BuildCommand : HSCommand("build") {
    init {
        Syntax {
            TagStore(player).use {
                val state = it.read(StorePlayerState::class)
                val world = getWorld(state.id)
                it.write(state.withMode(ModeHandler.Mode.BUILD))
                world.setInstanceToBuild(player)
            }
        }
    }
}

class DevCommand : HSCommand("dev") {
    init {
        Syntax {
            TagStore(player).use {
                val state = it.read(StorePlayerState::class)
                val world = getWorld(state.id)
                it.write(state.withMode(ModeHandler.Mode.DEV))
                world.setInstanceToDev(player)
            }
        }
    }
}

class WorldCommand : HSCommand("world") {
    init {
        addSubcommand(WorldCreateCommand())
        addSubcommand(WorldInvokeCommand())
    }
}
