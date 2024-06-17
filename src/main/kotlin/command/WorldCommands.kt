package userunp.hyperstom.command

import userunp.hyperstom.datastore.*
import userunp.hyperstom.code.getEvents
import userunp.hyperstom.world.*
import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import userunp.hyperstom.*
import userunp.hyperstom.code.dataLabel
import userunp.hyperstom.code.eventLabel
import java.util.UUID

data class WorldCommandContext(val ctx: CommandContext, val world: WorldManager, val player: Player)

abstract class WorldSubCommand(name: String) : HSCommand(name) {
    protected var requiredMode: WorldMode? = null
    protected var level: ContributorLevel? = null
    protected var inHubWorld = false
    protected var needsOwner = false

    inner class Syntax(
        arguments: Array<out Argument<*>>,
        executor: WorldCommandContext.() -> Unit
    ) : HSCommand.Syntax(arguments, exec@{
        val state = TagStore(player).use { it.read(StorePlayerState::class) }
        if (inHubWorld && state.id != HUB_WORLD_ID) throw CommandException("you cannot use this command here!")
        if (requiredMode != null && state.mode != requiredMode) throw CommandException("Must be in $requiredMode mode!")
        val world = worlds[state.id]
        if (world == null) {
            player.kick("World id ${state.id} does not exist!")
            return@exec
        }
        if (needsOwner) if (world.info.owner != player.uuid) throw CommandException("Only the owner can run this command!")
        if (level != null) {
            val contributors = PersistentStore(world).use { it.read(StoreWorldContributors::class) }
            if (!contributors.hasPerm(player.uuid, level!!)) throw CommandException("Insufficient permission!")
        }
        executor(WorldCommandContext(ctx, world, player))
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
            val files = defaultWorldFiles(WorldInfo("${player.username}'s world", player.uuid, null))
            writeWorldArchive(id, files)
            worlds[id] = WorldManager(id, files)
            player.sendMessage(Component.text("Finished creating world $id."))
        }
    }
}

object WorldInvokeCommand : WorldSubCommand("invoke") {
    init {
        for (event in getEvents()) {
            Syntax(arrayOf(ArgumentType.Literal(event.name))) {
                val code = readWorldCode(world)
                code({ world.play }, eventLabel(event), world)
            }
        }
        Syntax(arrayOf(ArgumentType.String("label"))) {
            val code = readWorldCode(world)
            code({ world.play }, dataLabel(ctx.get("label")), world)
        }
    }
}

object WorldLSLabelsCommand : WorldSubCommand("lslabels") {
    init {
        Syntax {
            player.sendMessage(MM.deserialize(StringBuilder().apply {
                appendLine("<red>LABELS: ------------")
                for (l in readWorldCode(world).getLabels()) {
                    appendLine("<dark_red> * <red>${l.name}:    <gray><i>${l.type}</i>")
                }
                appendLine("<red>-------------------")
            }.toString()))
        }
        Syntax(arrayOf(ArgumentType.Literal("verbose"))) {
            val code = readWorldCode(world)
            player.sendMessage(MM.deserialize(StringBuilder().apply {
                appendLine("<red>LABELS: ------------")
                for (l in code.getLabels()) {
                    val instList = code.getInstList(l)!!
                    appendLine("<dark_red> * <red>${l.name}:    <gray><i>${l.type}</i>")
                    for (i in instList) {
                        appendLine("<dark_red>        -> <gray><i>${i.props.name} @${i.target.name}</i>")
                    }
                }
                appendLine("<red>-------------------")
            }.toString()))
        }
    }
}

object WorldSaveCommand : WorldSubCommand("save") {
    init {
        Syntax {
            try {
                writeWorldArchive(world.id, world.files)
            } catch (e: Exception) { throw CommandException("IO error whilst saving world!") }
        }
    }
}

object WorldDataCommand : WorldSubCommand("data") {
    init {
        Syntax {
            val archive = readWorldArchive(world.id)
            val buildSize = archive.buildFile().use { it.available() }
            val dataSections = archive.varData.map.mapValues { it.value.size }
            val sectionsSize = dataSections.values.sum()
            player.sendMessage(MM.deserialize(StringBuilder().apply {
                appendLine("<red>DATA: --------------")
                appendLine("<dark_red> * <red>BUILD:    <gray><i>${bytesToHumanReadable(buildSize)}</i>")
                appendLine("<dark_red> * <red>VARDATA:    <gray><i>${bytesToHumanReadable(sectionsSize)}</i>")
                for (s in dataSections) {
                    appendLine("<dark_red>        * <red>${s.key}:  <gray><i>${bytesToHumanReadable(s.value)}</i>")
                }
                appendLine("Total size:    ${bytesToHumanReadable(sectionsSize + buildSize)}")
                appendLine("<red>-------------------")
            }.toString()))
        }
    }
}

