package hyperstom.infernity.dev.world

import hyperstom.infernity.dev.HUB_WORLD_ID
import hyperstom.infernity.dev.Utils
import hyperstom.infernity.dev.WORLDS_DIR
import net.hollowcube.polar.PolarLoader
import net.hollowcube.polar.PolarWorld
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.SharedInstance
import net.minestom.server.instance.block.Block
import net.minestom.server.world.DimensionType
import java.io.File
import java.util.Hashtable
import java.util.UUID

@Suppress("UnstableApiUsage")
class WorldManager(io: WorldArchiveIO) {
    private val id = io.id
    private val archive = io.read()
    val properties = archive.properties

    private val build = InstanceContainer(UUID.nameUUIDFromBytes("hs_world$id-build".toByteArray()), DimensionType.OVERWORLD, archive.build)
    val dev = InstanceContainer(UUID.nameUUIDFromBytes("hs_world$id-dev".toByteArray()), DimensionType.OVERWORLD, archive.dev)
    val play = SharedInstance(UUID.nameUUIDFromBytes("hs_world$id-play".toByteArray()), build)
    init {
        val instanceManager = MinecraftServer.getInstanceManager()
        dev.setGenerator { unit ->
            val modifier = unit.modifier()
            modifier.fillHeight(64, 65, Block.GRASS_BLOCK)
            modifier.fillHeight(58, 63, Block.STONE)
        }
        instanceManager.registerInstance(build)
        instanceManager.registerInstance(dev)
        instanceManager.registerSharedInstance(play)
    }

    fun setInstanceToBuild(player: Player) {
        player.setInstance(build, properties.spawnLoc?: buildSpawnPoint).thenAccept {
            player.gameMode = GameMode.CREATIVE
            player.sendMessage("Sent to build mode!")
        }
    }
    fun setInstanceToDev(player: Player) {
        player.setInstance(dev, devSpawnPoint).thenAccept {
            player.gameMode = GameMode.CREATIVE
            player.sendMessage("Sent to dev mode!")
        }
    }
    fun setInstanceToPlay(player: Player) {
        player.setInstance(play, properties.spawnLoc?: buildSpawnPoint).thenAccept {
            player.gameMode = GameMode.SURVIVAL
            player.sendMessage("Sent to play mode!")
        }
    }

    fun setDefaultInstance(event: PlayerLoginEvent) {
        event.setSpawningInstance(play)
        event.player.respawnPoint = properties.spawnLoc?: buildSpawnPoint
        event.player.gameMode = GameMode.SURVIVAL
    }

    companion object {
        val buildSpawnPoint = Pos(0.0, 65.0, 0.0)
        val devSpawnPoint = Pos(0.0, 65.5,  0.0)
        val worlds: Hashtable<Int, WorldManager> = Hashtable()
        fun initWorlds() {
            val dir = File(WORLDS_DIR)
            if (!dir.exists()) dir.mkdirs()
            else if (!dir.isDirectory) throw FileAlreadyExistsException(dir, null, "$WORLDS_DIR is not a directory!")
            for (str in dir.list()!!) {
                if (!str.endsWith(".tar.gz")) continue
                val id = Integer.parseInt(str.replace(".tar.gz", ""))
                if (worlds.contains(id)) throw RuntimeException("Duplicate world id ($id)!")
                worlds[id] = WorldManager(WorldArchiveIO(id))
            }
            if (!worlds.containsKey(HUB_WORLD_ID)) {
                val archiveIO = WorldArchiveIO(HUB_WORLD_ID)
                val properties = WorldSavedProperties("HUB WORLD", null, null, mapOf())
                archiveIO.write(WorldArchiveIO.Data(properties, PolarLoader(Utils.getResource("build")), DevSpaceLoader(PolarWorld())))
                worlds[HUB_WORLD_ID] = WorldManager(archiveIO)
            }
        }
    }
}
