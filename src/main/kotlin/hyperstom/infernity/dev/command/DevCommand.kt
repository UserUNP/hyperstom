package hyperstom.infernity.dev.command

import hyperstom.infernity.dev.world.mode.ModeHandler
import hyperstom.infernity.dev.tagstore.StoreWorldState
import hyperstom.infernity.dev.world.WorldManager
import net.minestom.server.utils.validate.Check

class DevCommand : HSCommand("dev") {
    init {
        Syntax(emptyArray()) { player, store, _ ->
            var state = store.read(StoreWorldState::class)
            if (state.mode == ModeHandler.Mode.DEV) throw RuntimeException("Already in dev mode!")
            store.write(state.withMode(ModeHandler.Mode.DEV).also { state = it })
            val world = WorldManager.worlds[state.id]
            Check.notNull(world, "World with id ${state.id} does not exist!")
            world!!.setInstanceToDev(player)
        }
    }
}
