package dev.bedcrab.hyperstom

import dev.bedcrab.hyperstom.code.initCodeBlocks
import dev.bedcrab.hyperstom.command.initCommands
import dev.bedcrab.hyperstom.datastore.StorePlayerState
import dev.bedcrab.hyperstom.datastore.TagStore
import dev.bedcrab.hyperstom.listener.ModeHandler
import dev.bedcrab.hyperstom.listener.initModeHandlers
import dev.bedcrab.hyperstom.world.*
import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.extras.MojangAuth
import java.io.File
import java.util.UUID
import kotlin.concurrent.thread

private const val VERSION = "1.0.0-alpha"
private val LOGGER = KotlinLogging.logger {}

val HUB_WORLD_INFO = WorldInfo("HUB WORLD", null, null)
val HUB_WORLD_ID = UUID(0, 0)

fun main() {
    LOGGER.info { "\t*** Hyperstom - v$VERSION ***" }
    val server = MinecraftServer.init()

    initCodeBlocks()
    LOGGER.info { "\t> Initialized code blocks." }
    initInventories()
    LOGGER.info { "\t> Initialized inventories." }
    initWorlds()
    LOGGER.info { "\t> Initialized worlds." }
    initMCServer()
    LOGGER.info { "\t> Initialized MC server." }

    server.start("0.0.0.0", 25565)
}

fun initWorlds() {
    val dir = File(WORLDS_DIR)
    if (!dir.exists()) dir.mkdirs()
    else if (!dir.isDirectory) throw FileAlreadyExistsException(dir, null, "$WORLDS_DIR is not a directory!")
    for (str in dir.list()!!) {
        if (!str.endsWith(".$WORLDS_EXT")) continue
        val id = UUID.fromString(str.replace(".$WORLDS_EXT", ""))
        if (worlds.contains(id)) throw RuntimeException("Duplicate world id ($id)!")
        worlds[id] = WorldManager(id, readWorldArchive(id))
    }
    if (!worlds.containsKey(HUB_WORLD_ID)) {
        LOGGER.warn { "Couldn't find hub world. Creating." }
        val files = WorldArchiveFiles.default(HUB_WORLD_INFO)
        writeWorldArchive(HUB_WORLD_ID, files)
        worlds[HUB_WORLD_ID] = WorldManager(HUB_WORLD_ID, files)
    }
}

fun initMCServer() {
    val eventHandler = MinecraftServer.getGlobalEventHandler()
    MinecraftServer.setBrandName("Hyperstom")
    MojangAuth.init()
    initCommands()
    initModeHandlers(eventHandler)
    eventHandler.addListener(AsyncPlayerConfigurationEvent::class.java, ::configurePlayer)
    eventHandler.addListener(PlayerDisconnectEvent::class.java, ::disconnectPlayer)
    Runtime.getRuntime().addShutdownHook(thread(false) {
        TODO("World saving")
    })
}

private fun configurePlayer(e: AsyncPlayerConfigurationEvent) {
    TagStore(e.player).use { it.write(StorePlayerState(ModeHandler.Mode.PLAY.ordinal, HUB_WORLD_ID)) }
    val world = worlds[HUB_WORLD_ID] ?: throw RuntimeException("Hub world ($HUB_WORLD_ID) does not exist!")
    e.spawningInstance = world.play
    e.player.respawnPoint = world.info.spawnLoc ?: BUILD_SPAWN_POINT
    e.player.gameMode = GameMode.SURVIVAL
}

private fun disconnectPlayer(e: PlayerDisconnectEvent) {
    if (e.player.instance.entities.size <= 1) {
        val state = TagStore(e.player).use { it.read(StorePlayerState::class) }
        worlds[state.id]?.let { writeWorldArchive(state.id, it.files) }
    }
}
