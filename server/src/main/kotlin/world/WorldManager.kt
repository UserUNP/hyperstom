@file:Suppress("UnstableApiUsage")
@file:OptIn(DelicateCoroutinesApi::class)

package dev.bedcrab.hyperstom.world

import dev.bedcrab.hyperstom.code.*
import dev.bedcrab.hyperstom.datastore.PersistentStore
import dev.bedcrab.hyperstom.datastore.StoreDataProvider
import dev.bedcrab.hyperstom.datastore.StoreWorldCode
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.InstanceManager
import net.minestom.server.instance.SharedInstance
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.time.TimeUnit
import net.minestom.server.world.DimensionType
import java.io.File
import java.util.UUID

const val WORLDS_DIR = "worlds"
const val WORLDS_EXT = "hstom"
val HUB_WORLD_ID: UUID = UUID(0, 0)
val HUB_WORLD_INFO = WorldInfo("HUB WORLD", null, null)
private val LOGGER = KotlinLogging.logger {}

val BUILD_SPAWN_POINT = Pos(0.0, 65.0, 0.0)
val DEV_SPAWN_POINT = Pos(0.0, 65.5,  0.0)

private lateinit var instanceManager: InstanceManager
private lateinit var hubInstance: InstanceContainer
private lateinit var worldSaveJob: Job

fun initWorlds() {
    instanceManager = MinecraftServer.getInstanceManager()
    hubInstance = instanceManager.createInstanceContainer(DimensionType.OVERWORLD)
    hubInstance.setGenerator { unit -> unit.modifier().fillHeight(49, 50, Block.GRASS_BLOCK) }

    val dir = File(WORLDS_DIR)
    if (!dir.exists()) dir.mkdirs()
    else if (!dir.isDirectory) throw FileAlreadyExistsException(dir, null, "$WORLDS_DIR is not a directory!")
    for (str in dir.list()!!) {
        if (!str.endsWith(".$WORLDS_EXT")) continue
        val id = UUID.fromString(str.replace(".$WORLDS_EXT", ""))
        if (WorldManager.worlds.contains(id)) throw RuntimeException("Duplicate world id ($id)!")
        WorldManager.worlds[id] = WorldManager(id, readWorldArchive(id))
    }
    if (!WorldManager.worlds.containsKey(HUB_WORLD_ID)) {
        LOGGER.warn { "Couldn't find hub world. Creating." }
        val files = WorldArchiveFiles.default(HUB_WORLD_INFO)
        writeWorldArchive(HUB_WORLD_ID, files)
        WorldManager.worlds[HUB_WORLD_ID] = WorldManager(HUB_WORLD_ID, files)
    }

    worldSaveJob = GlobalScope.launch {
        while (true) {
            try {
                delay(TimeUnit.MINUTE.duration.toMillis() * 5)
                saveAll()
            } catch (e: CancellationException) {
                saveAll()
                throw e
            }
        }
    }
}

private fun saveAll() {
    LOGGER.info { "Saving worlds..." }
    for ((id, world) in WorldManager.worlds) writeWorldArchive(id, world.files)
}

suspend fun cancelWorldSaving() {
    worldSaveJob.cancelAndJoin()
}

class WorldManager(val id: UUID, val files: WorldArchiveFiles) : StoreDataProvider<WorldVarData> {
    val info = files.info
    private val build = InstanceContainer(UUID.nameUUIDFromBytes("$id.$WORLDS_EXT/$id.build".toByteArray()), DimensionType.OVERWORLD, files.build)
    private val dev = InstanceContainer(UUID.nameUUIDFromBytes("$WORLDS_EXT/$id.dev".toByteArray()), DimensionType.OVERWORLD, files.dev)
    val play = SharedInstance(UUID.nameUUIDFromBytes("$WORLDS_EXT/$id.play".toByteArray()), build)
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
        LOGGER.info { "Registered world $id." }

        val testInstList = mutableListOf(Instruction(InstProperties.`bruh moner`))
        val testTypeEntry = getTypeEntry(EVENT_TYPE, HSEvent.WORLD_INITIALIZATION)
        PersistentStore(this).use {
            it.write(StoreWorldCode(mutableMapOf(
                testTypeEntry to testInstList
            )))
        }
    }

    override fun data() = files.varData

    fun setInstanceToBuild(player: Player) {
        player.setInstance(build, info.spawnLoc ?: BUILD_SPAWN_POINT).thenAccept {
            player.gameMode = GameMode.CREATIVE
            player.sendMessage("Sent to build mode!")
        }
    }
    fun setInstanceToDev(player: Player) {
        player.setInstance(dev, DEV_SPAWN_POINT).thenAccept {
            player.gameMode = GameMode.CREATIVE
            player.sendMessage("Sent to dev mode!")
        }
    }
    fun setInstanceToPlay(player: Player) {
        player.setInstance(play, info.spawnLoc ?: BUILD_SPAWN_POINT).thenAccept {
            player.gameMode = GameMode.SURVIVAL
            player.sendMessage("Sent to play mode!")
        }
    }

    companion object {
        val worlds = mutableMapOf<UUID, WorldManager>()
    }
}
