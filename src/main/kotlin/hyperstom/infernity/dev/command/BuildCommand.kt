package hyperstom.infernity.dev.command

import hyperstom.infernity.dev.world.mode.ModeHandler
import hyperstom.infernity.dev.tagstore.StoreWorldState
import hyperstom.infernity.dev.world.WorldManager
import net.minestom.server.utils.validate.Check

class BuildCommand : HSCommand("build") {
    init {
        Syntax { player, store, _ ->
            var state = store.read(StoreWorldState::class)
            if (state.mode == ModeHandler.Mode.BUILD.ordinal) throw RuntimeException("Already in build mode!")
            store.write(state.withMode(ModeHandler.Mode.BUILD).also { state = it })
            val world = WorldManager.worlds[state.id]
            Check.notNull(world, "World with id ${state.id} does not exist!")
            world!!.setInstanceToBuild(player)
        }
    }
}
