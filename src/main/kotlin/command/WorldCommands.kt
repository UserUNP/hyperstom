package ma.userunp.hyperstom.command

import ma.userunp.hyperstom.*
import ma.userunp.hyperstom.code.*
import ma.userunp.hyperstom.world.*
import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import java.util.*
import kotlin.time.measureTime

data class WorldCommandContext(val ctx: CommandContext, val world: WorldManager, val player: Player)

abstract class WorldSubCommand(name: String) : HSCommand(name) {
    protected var requiredMode: WorldMode? = null
    protected var level: ContribLevel? = null
    protected var inHubWorld = false
    protected var needsOwner = false

    inner class Syntax(
        arguments: Array<out Argument<*>>,
        executor: WorldCommandContext.() -> Unit
    ) : HSCommand.Syntax(arguments, exec@{
        val pUuid = player.uuid
        playerStates[pUuid]?.run {
            requiredMode?.let { if (mode != it) throw CommandException("Must be in $requiredMode mode!") }
            if (inHubWorld && world.id != HUB_WORLD_ID) throw CommandException("you cannot use this command here!")
            world.files.run {
                if (needsOwner && info.owner != pUuid) throw CommandException("Only the owner can run this command!")
                level?.let { if (contrib.hasPerm(pUuid, it)) throw CommandException("Insufficient permission!") }
            }
            executor(WorldCommandContext(ctx, world, player))
        }
    }) {
        constructor(executor: WorldCommandContext.() -> Unit) : this(emptyArray(), executor)
    }
}

object WorldCreateCommand : WorldSubCommand("create") {
    init {
        inHubWorld = true
        requiredMode = WorldMode.PLAY
        Syntax {
            player.sendMessage(Component.text("Creating world..."))
            val id = UUID.randomUUID()
            val files = defaultWorldFiles(WorldInfo("${player.username}'s world", player.uuid, BUILD_SPAWN_POINT))
            writeWorldArchive(id, files)
            worlds[id] = WorldManager(id, files)
            player.sendMessage(Component.text("Finished creating world $id."))
        }
    }
}

object WorldInvokeCommand : WorldSubCommand("invoke") {
    init {
        for (event in CodeEvent.entries) {
            Syntax(arrayOf(ArgumentType.Literal(event.id))) {
                world.files.code.invoke({ world.play }, eventLabel(event), world.runtimeInvoker)
            }
        }
        Syntax(arrayOf(ArgumentType.String("label"))) {
            world.files.code.invoke({ world.play }, dataLabel(ctx.get("label")), world.runtimeInvoker)
        }
    }
}

object WorldLSLabelsCommand : WorldSubCommand("lslabels") {
    init {
        Syntax { player.sendMessage(MM.deserialize(buildString {
            appendLine("<red>LABELS: ------------")
            for (l in world.files.code.getLabels()) {
                appendLine("<dark_red> * <red>${l.label}:    <i>(${l.type})</i>")
            }
            appendLine("<red>-------------------")
        })) }
        Syntax(arrayOf(ArgumentType.Literal("verbose"))) { player.sendMessage(MM.deserialize(buildString {
            val code = world.files.code
            appendLine("<red>LABELS: ------------")
            for (l in code.getLabels()) {
                val instList = code.resolveLabel(l)
                appendLine("<dark_red> * <red>${l.label}:    <i>(${l.type})</i>")
                for (i in instList) {
                    if (i.target.targetClass() != TargetClass.NONE) {
                        appendLine("<dark_red>        -> <gray>${i.type.name} <i>@${i.target.name}</i>")
                    } else appendLine("<dark_red>        -> <gray>${i.type.name}")
                }
            }
            appendLine("<red>-------------------")
        })) }
    }
}

object WorldSaveCommand : WorldSubCommand("save") {
    init {
        Syntax {
            try {
                player.sendMessage(MM.deserialize("<red>Saved ${world.id} <dark_red> - ${measureTime {
                    writeWorldArchive(world.id, world.files)
                }}"))
            } catch (e: Exception) { throw CommandException("IO error whilst saving world!") }
        }
    }
}
