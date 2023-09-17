package hyperstom.infernity.dev.command

import hyperstom.infernity.dev.world.mode.ModeHandler
import hyperstom.infernity.dev.tagstore.StoreWorldState
import hyperstom.infernity.dev.world.WorldManager
import net.minestom.server.utils.validate.Check

class PlayCommand : HSCommand("play") {
    init {
        Syntax(emptyArray()) { player, store, _ ->
            var state = store.read(StoreWorldState::class)
            if (state.mode == ModeHandler.Mode.PLAY) throw RuntimeException("Already in play mode!")
            store.write(state.withMode(ModeHandler.Mode.PLAY).also { state = it })
            val world = WorldManager.worlds[state.id]
            Check.notNull(world, "World with id ${state.id} does not exist!")
            world!!.setInstanceToPlay(player)
        }
    }
}
