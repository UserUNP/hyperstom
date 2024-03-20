package dev.bedcrab.hyperstom.command

import dev.bedcrab.hyperstom.*
import dev.bedcrab.hyperstom.code.EVENT_BLOCK_TYPE
import dev.bedcrab.hyperstom.code.HSEvent
import dev.bedcrab.hyperstom.code.rootCodeBlockEntry
import dev.bedcrab.hyperstom.datastore.*
import dev.bedcrab.hyperstom.ModeHandler
import dev.bedcrab.hyperstom.world.*
import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import java.util.UUID

abstract class WorldSubCommand(name: String) : HSCommand(name) {
    protected var requiredMode: ModeHandler.Mode? = null
    protected var level: ContributorLevel? = null
    protected var inHubWorld = false
    protected var needsOwner = false

    inner class Syntax(arguments: Array<out Argument<*>>, executor: WorldCommandContext.() -> Unit) : HSCommand.Syntax(arguments, exec@{
        val state = TagStore(player).use { it.read(StorePlayerState::class) }
        if (inHubWorld && state.id != HUB_WORLD_ID) throw RuntimeException("you cannot use this command here!")
        if (requiredMode != null && state.mode != requiredMode) throw RuntimeException("Must be in $requiredMode mode!")
        val world = worlds[state.id]
        if (world == null) {
            player.kick("World id ${state.id} does not exist!")
            return@exec
        }
        if (needsOwner) if (world.info.owner != player.uuid) throw RuntimeException("Only the owner can run this command!")
        if (level != null) {
            val contributors = PersistentStore(world).use { it.read(StoreWorldContributors::class) }
            if (!contributors.hasPerm(player.uuid, level!!)) throw RuntimeException("Insufficient permission!")
        }
        executor(WorldCommandContext(context, world.info, player))
    }) {
        constructor(executor: WorldCommandContext.() -> Unit) : this(emptyArray(), executor)
    }

    data class WorldCommandContext(val context: CommandContext, val world: WorldInfo, val player: Player)
}

class WorldCreateCommand : WorldSubCommand("create") {
    init {
        inHubWorld = true
        requiredMode = ModeHandler.Mode.PLAY
        Syntax {
            player.sendMessage { Component.text("Creating world...") }
            val files = WorldArchiveFiles.default(WorldInfo("${player.username}'s world", player.uuid, null))
            val id = UUID.randomUUID()
            writeWorldArchive(id, files)
            worlds[id] = WorldManager(id, files)
            player.sendMessage { Component.text("Finished creating world $id.") }
        }
    }
}

class WorldInvokeCommand : WorldSubCommand("invoke") {
    init {
        Syntax(arrayOf(ArgumentType.Enum("event", HSEvent::class.java))) {
            val event = context.get<HSEvent>("event")
            val state = TagStore(player).use { it.read(StorePlayerState::class) }
            val world = getWorld(state.id)
            val code = PersistentStore(world).use { it.read(StoreWorldCode::class) }
            code({ world.play }, rootCodeBlockEntry(EVENT_BLOCK_TYPE, event), world, mutableListOf(player))
        }
    }
}
