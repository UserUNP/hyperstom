package hyperstom.infernity.dev.command.world

import hyperstom.infernity.dev.getResource
import hyperstom.infernity.dev.world.WorldArchiveIO
import hyperstom.infernity.dev.world.WorldManager
import hyperstom.infernity.dev.world.WorldSavedProperties
import net.hollowcube.polar.PolarLoader
import net.hollowcube.polar.PolarWorld

class WorldCreateCommand : WorldSubCommand("create", null) {
    init {
        Syntax { player, _, _ ->
            player.sendMessage("Creating new world..")
            val id = WorldManager.worlds.size
            val archiveIO = WorldArchiveIO(id)
            val properties = WorldSavedProperties("${player.username}'s world", player.uuid, WorldManager.buildSpawnPoint, mapOf())
            archiveIO.write(WorldArchiveIO.Data(properties, PolarLoader(getResource("build")), PolarLoader(PolarWorld())))
            WorldManager.worlds[id] = WorldManager(archiveIO)
            player.sendMessage("Finished creating world $id")
        }
    }
}
