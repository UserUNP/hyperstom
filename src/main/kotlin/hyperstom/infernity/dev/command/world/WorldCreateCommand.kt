package hyperstom.infernity.dev.command.world

import hyperstom.infernity.dev.HUB_WORLD_ID
import hyperstom.infernity.dev.Utils
import hyperstom.infernity.dev.tagstore.StoreWorldState
import hyperstom.infernity.dev.world.DevSpaceLoader
import hyperstom.infernity.dev.world.WorldArchiveIO
import hyperstom.infernity.dev.world.WorldManager
import hyperstom.infernity.dev.world.WorldSavedProperties
import hyperstom.infernity.dev.world.mode.ModeHandler
import net.hollowcube.polar.PolarLoader
import net.hollowcube.polar.PolarWorld

class WorldCreateCommand : WorldSubCommand("create") {
    init {
        Syntax { ctx ->
            val state = ctx.store.read(StoreWorldState::class)
            if (state.id != HUB_WORLD_ID) throw RuntimeException("You can only use this command in the hub!")
            if (state.mode != ModeHandler.Mode.PLAY) throw RuntimeException("You can only use this command whilst in play mode!")
            ctx.player.sendMessage("Creating new world..")
            val id = WorldManager.worlds.size
            val archiveIO = WorldArchiveIO(id)
            val properties = WorldSavedProperties("${ctx.player.username}'s world", ctx.player.uuid, WorldManager.buildSpawnPoint, mapOf())
            archiveIO.write(WorldArchiveIO.Data(properties, PolarLoader(Utils.getResource("build")), DevSpaceLoader(PolarWorld())))
            WorldManager.worlds[id] = WorldManager(archiveIO)
            ctx.player.sendMessage("Finished creating world $id")
        }
    }
}
