package dev.bedcrab.hyperstom.command

import dev.bedcrab.hyperstom.datastore.StorePlayerState
import dev.bedcrab.hyperstom.ModeHandler
import dev.bedcrab.hyperstom.datastore.TagStore
import dev.bedcrab.hyperstom.world.BUILD_SPAWN_POINT
import dev.bedcrab.hyperstom.world.DEV_SPAWN_POINT
import dev.bedcrab.hyperstom.world.WorldManager
import dev.bedcrab.hyperstom.world.getWorld
import io.github.oshai.kotlinlogging.KotlinLogging
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
        AboutCommand(), WorldCommand(),
        PlayCommand(), BuildCommand(), DevCommand(),
    )) cmdManager.register(cmd)
    LOGGER.info { "Registered ${cmdManager.commands.size} commands." }
}

private fun defaultExecutor(sender: CommandSender) = sender.sendMessage("Invalid syntax!")
private fun setMode(store: TagStore, mode: ModeHandler.Mode): WorldManager {
    val state = store.read(StorePlayerState::class)
    val world = getWorld(state.id)
    store.write(state.withMode(mode))
    return world
}

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
            val world = TagStore(player).use { setMode(it, ModeHandler.Mode.PLAY) }
            player.setGameMode(GameMode.SURVIVAL)
            player.setInstance(world.play, world.info.spawnLoc ?: BUILD_SPAWN_POINT)
        }
    }
}

class BuildCommand : HSCommand("build") {
    init {
        Syntax {
            val world = TagStore(player).use { setMode(it, ModeHandler.Mode.BUILD) }
            player.setGameMode(GameMode.CREATIVE)
            player.setInstance(world.build, world.info.spawnLoc ?: BUILD_SPAWN_POINT)
        }
    }
}

class DevCommand : HSCommand("dev") {
    init {
        Syntax {
            val world = TagStore(player).use { setMode(it, ModeHandler.Mode.DEV) }
            player.setGameMode(GameMode.CREATIVE)
            player.setInstance(world.dev, world.info.spawnLoc ?: DEV_SPAWN_POINT)
        }
    }
}

class WorldCommand : HSCommand("world") {
    init {
        addSubcommand(WorldCreateCommand())
        addSubcommand(WorldInvokeCommand())
    }
}
