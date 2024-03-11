package dev.bedcrab.hyperstom

import dev.bedcrab.hyperstom.command.initCommands
import dev.bedcrab.hyperstom.world.BUILD_SPAWN_POINT
import dev.bedcrab.hyperstom.world.WorldInfo
import dev.bedcrab.hyperstom.world.worlds
import dev.bedcrab.hyperstom.world.writeWorldArchive
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.extras.MojangAuth
import java.util.UUID
import kotlin.concurrent.thread

val HUB_WORLD_ID: UUID = UUID(0, 0)
val HUB_WORLD_INFO = WorldInfo("HUB WORLD", null, null)

fun startMinecraftServer() {
    val server = MinecraftServer.init()
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
    server.start("0.0.0.0", 25565)
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
