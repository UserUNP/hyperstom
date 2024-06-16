package userunp.hyperstom.command

import userunp.hyperstom.datastore.StorePlayerState
import userunp.hyperstom.datastore.TagStore
import userunp.hyperstom.world.BUILD_SPAWN_POINT
import userunp.hyperstom.world.DEV_SPAWN_POINT
import userunp.hyperstom.world.WorldManager
import userunp.hyperstom.world.getWorld
import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import userunp.hyperstom.WorldMode

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
private fun setModeGetWorld(store: TagStore, mode: WorldMode): WorldManager {
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

object AboutCommand : Command("about") {
    init {
        setDefaultExecutor { sender, _ -> sender.sendMessage("https://github.com/userunp/hyperstom") }
    }
}

object PlayCommand : HSCommand("play") {
    init {
        Syntax {
            val world = TagStore(player).use { setModeGetWorld(it, WorldMode.PLAY) }
            player.setGameMode(GameMode.SURVIVAL)
            player.inventory.clear()
            player.setInstance(world.play, world.info.spawnLoc ?: BUILD_SPAWN_POINT)
        }
    }
}

object BuildCommand : HSCommand("build") {
    init {
        Syntax {
            val world = TagStore(player).use { setModeGetWorld(it, WorldMode.BUILD) }
            player.setGameMode(GameMode.CREATIVE)
            player.inventory.clear()
            player.setInstance(world.build, world.info.spawnLoc ?: BUILD_SPAWN_POINT)
        }
    }
}

object DevCommand : HSCommand("dev") {
    init {
        Syntax {
            val world = TagStore(player).use { setModeGetWorld(it, WorldMode.DEV) }
            player.setGameMode(GameMode.CREATIVE)
            val inventory = player.inventory
            inventory.clear()
            player.setInstance(world.dev, world.info.spawnLoc ?: DEV_SPAWN_POINT)
            player.sendMessage("The devspace is not finished!")
        }
    }
}

object WorldCommand : HSCommand("world") {
    init {
        addSubcommand(WorldCreateCommand)
        addSubcommand(WorldInvokeCommand)
        addSubcommand(WorldLSLabelsCommand)
        addSubcommand(WorldSaveCommand)
        addSubcommand(WorldDataCommand)
    }
}
