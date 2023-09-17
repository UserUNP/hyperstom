package hyperstom.infernity.dev.command.world

import hyperstom.infernity.dev.command.HSCommand
import hyperstom.infernity.dev.tagstore.StoreWorldState
import hyperstom.infernity.dev.tagstore.TagStore
import hyperstom.infernity.dev.world.ContributorLevel
import hyperstom.infernity.dev.world.WorldManager
import hyperstom.infernity.dev.world.WorldSavedProperties
import hyperstom.infernity.dev.world.mode.ModeHandler
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.entity.Player

abstract class WorldSubCommand(name: String) : HSCommand(name) {
    protected var requiredMode: ModeHandler.Mode? = null
    protected var level: ContributorLevel? = null
    protected var needsOwner = false

    inner class Syntax(vararg arguments: Argument<*>, executor: WorldCommandExecutor) : HSCommand.Syntax(arguments, exec@ { player, store, context ->
        val state = store.read(StoreWorldState::class)
        if (requiredMode != null) if (state.mode != requiredMode) throw RuntimeException("Must be in $requiredMode mode!")
        val world = WorldManager.worlds[state.id]
        if (world == null) {
            player.kick("World id ${state.id} does not exist!")
            return@exec
        }
        if (needsOwner) if (world.properties.owner != player.uuid) throw RuntimeException("Only the owner can run this command!")
        if (level != null) if (world.properties.contributors[player.uuid] != level) throw RuntimeException("Insufficient permissions!")
        executor.apply(WorldCommandContext(context.input, world.properties, player, store))
    })

    class WorldCommandContext(input: String, val world: WorldSavedProperties, val player: Player, val store: TagStore) : CommandContext(input)

    fun interface WorldCommandExecutor {
        fun apply(context: WorldCommandContext)
    }
}
