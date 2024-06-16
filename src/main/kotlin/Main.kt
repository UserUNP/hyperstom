package userunp.hyperstom

import userunp.hyperstom.command.initCommands
import userunp.hyperstom.datastore.StorePlayerState
import userunp.hyperstom.datastore.TagStore
import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.utils.identity.NamedAndIdentified
import userunp.hyperstom.world.*
import java.util.UUID
import kotlin.time.measureTime

private const val VERSION = "1.0.0-alpha"
private val LOGGER = KotlinLogging.logger {}

val HUB_WORLD_INFO = WorldInfo("HUB WORLD", null, null)
val HUB_WORLD_ID = UUID(0, 0)
private val HUB_WORLD_STATE = StorePlayerState(WorldMode.PLAY.ordinal, HUB_WORLD_ID)

fun main() {
    LOGGER.info { "\t*** Hyperstom - v$VERSION ***" }
    val server = MinecraftServer.init()
    val eventHandler = MinecraftServer.getGlobalEventHandler()
    LOGGER.info { "\t> Took ${measureTime {
        LOGGER.info { "\t> Initializing data." }
        initData()
        LOGGER.info { "\t> Initializing worlds." }
        initWorlds(eventHandler)
        LOGGER.info { "\t> Initializing MC server." }
        initMCServer(eventHandler)
        server.start("0.0.0.0", 25565)
        MinecraftServer.getSchedulerManager().buildShutdownTask(::shutdownWorlds)
    }} seconds." }
}


fun initMCServer(eventHandler: EventNode<Event>) {
    MojangAuth.init()
    initCommands()
    eventHandler.addListener(AsyncPlayerConfigurationEvent::class.java, ::configurePlayer)
    eventHandler.addListener(ServerListPingEvent::class.java, ::pingEntries)
    MinecraftServer.setBrandName("Hyperstom")
}


private fun pingEntries(e: ServerListPingEvent) = e.responseData.addEntries(
    NamedAndIdentified.named("Hyperstom - v$VERSION"),
)

private fun configurePlayer(e: AsyncPlayerConfigurationEvent) {
    TagStore(e.player).use { it.write(HUB_WORLD_STATE) }
    val world = worlds[HUB_WORLD_ID] ?: throw RuntimeException("Hub world ($HUB_WORLD_ID) does not exist!")
    e.spawningInstance = world.play
    e.player.respawnPoint = world.info.spawnLoc ?: BUILD_SPAWN_POINT
    e.player.gameMode = GameMode.SURVIVAL
}
