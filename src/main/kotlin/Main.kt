package ma.userunp.hyperstom

import io.github.oshai.kotlinlogging.KotlinLogging
import ma.userunp.hyperstom.command.initCommands
import ma.userunp.hyperstom.world.*
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.utils.identity.NamedAndIdentified
import java.util.*
import kotlin.time.measureTime

private const val VERSION = "1.0.0-alpha"
private val LOGGER = KotlinLogging.logger {}

val HUB_WORLD_INFO = WorldInfo("HUB WORLD", null, BUILD_SPAWN_POINT)
val HUB_WORLD_ID = UUID(0, 0)
private val PING_ENTIRES = arrayOf(
    NamedAndIdentified.named("Hyperstom - v$VERSION"),
)

val playerStates = mutableMapOf<UUID, PlayerState>()

fun main() {
    LOGGER.info { "\t*** Hyperstom - v$VERSION ***" }
    val dur = measureTime {
        val server = MinecraftServer.init()
        LOGGER.info { "\t> Initializing worlds." }
        try { initWorlds() } catch (e: Exception) { LOGGER.error(e) { "Error whilst initializing worlds!" } }
        LOGGER.info { "\t> Initializing MC server." }
        initMCServer()
        server.start("0.0.0.0", 25565)
        MinecraftServer.getSchedulerManager().buildShutdownTask(::shutdownWorlds)
    }
    LOGGER.info { "\t> Took $dur" }
}

private fun configurePlayer(e: AsyncPlayerConfigurationEvent) {
    val world = worlds[HUB_WORLD_ID] ?: throw RuntimeException("Hub world ($HUB_WORLD_ID) does not exist!")
    playerStates[e.player.uuid] = PlayerState(WorldMode.PLAY, world)
    e.spawningInstance = world.play
    e.player.respawnPoint = world.files.info.spawnLoc
    e.player.gameMode = GameMode.SURVIVAL
}

private fun handlePlayerDisconnect(e: PlayerDisconnectEvent) {
    playerStates.remove(e.player.uuid)
}

fun initMCServer() {
    initExceptionHandler()
    MojangAuth.init()
    initCommands()
    val eventHandler = MinecraftServer.getGlobalEventHandler()
    eventHandler.addListener(AsyncPlayerConfigurationEvent::class.java, ::configurePlayer)
    eventHandler.addListener(PlayerDisconnectEvent::class.java, ::handlePlayerDisconnect)
    eventHandler.addListener(ServerListPingEvent::class.java) { it.responseData.addEntries(*PING_ENTIRES) }
    MinecraftServer.setBrandName("Hyperstom")
}
