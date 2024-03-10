package dev.bedcrab.hyperstom

import dev.bedcrab.hyperstom.command.initCommands
import dev.bedcrab.hyperstom.datastore.StorePlayerState
import dev.bedcrab.hyperstom.datastore.TagStore
import dev.bedcrab.hyperstom.world.*
import dev.bedcrab.hyperstom.world.initModeHandlers
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.tag.Tag
import kotlin.concurrent.thread

val TAG_STORE_ROOT = Tag.NBT("HYPERSTOM")
private val LOGGER = KotlinLogging.logger {}
private const val VERSION = "1.0.0-alpha"

fun main() {
    val server = MinecraftServer.init()
    val eventHandler = MinecraftServer.getGlobalEventHandler()
    MinecraftServer.setBrandName("Hyperstom")
    LOGGER.info { "\t*** Hyperstom - v$VERSION ***" }
    LOGGER.info { "\t> Initializing..." }

    MojangAuth.init()
    initWorlds()
    initModeHandlers(eventHandler)
    initCommands(MinecraftServer.getCommandManager())
    eventHandler.addListener(AsyncPlayerConfigurationEvent::class.java, ::configurePlayer)
    eventHandler.addListener(PlayerDisconnectEvent::class.java, ::disconnectPlayer)

    Runtime.getRuntime().addShutdownHook(thread(false) {
        LOGGER.info { "\t> Exiting..." }
        runBlocking {
            cancelWorldSaving()
            LOGGER.info { "\t> Exited." }
        }
    })

    LOGGER.info { "\t> Initialization complete." }
    server.start("0.0.0.0", 25565)
}

fun configurePlayer(e: AsyncPlayerConfigurationEvent) {
    TagStore(e.player).use { it.write(StorePlayerState(ModeHandler.Mode.PLAY, HUB_WORLD_ID)) }
    val world = WorldManager.worlds[HUB_WORLD_ID] ?: throw RuntimeException("Hub world ($HUB_WORLD_ID) does not exist!")
    e.spawningInstance = world.play
    e.player.respawnPoint = world.info.spawnLoc ?: BUILD_SPAWN_POINT
    e.player.gameMode = GameMode.SURVIVAL
}

fun disconnectPlayer(e: PlayerDisconnectEvent) {
    if (e.player.instance.entities.size <= 1) {
        val state = TagStore(e.player).use { it.read(StorePlayerState::class) }
        WorldManager.worlds[state.id]?.let { writeWorldArchive(state.id, it.files) }
    }
}
