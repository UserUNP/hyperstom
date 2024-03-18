package dev.bedcrab.hyperstom.listener

import dev.bedcrab.hyperstom.datastore.StorePlayerState
import dev.bedcrab.hyperstom.datastore.TagStore
import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.coordinate.Point
import net.minestom.server.event.Event
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.instance.Instance

private val LOGGER = KotlinLogging.logger {}

fun initModeHandlers(parentNode: EventNode<in Event>) {
    for (mode in ModeHandler.Mode.entries) {
        mode.init()
        parentNode.addChild(mode.eventNode)
    }
    LOGGER.info { "Registered mode handlers." }
}

interface ModeHandler {
    val eventNode: EventNode<*>
    fun init()

    enum class Mode(private val handler: ModeHandler) : ModeHandler {
        PLAY(PlayMode),
        BUILD(BuildMode),
        DEV(DevMode),
        ;
        override val eventNode = handler.eventNode
        override fun init() = handler.init()
        override fun toString() = handler.toString()
    }
}

private object PlayMode : ModeHandler {
    // entities should have the tag to be considered a part of the plot anyway
    override val eventNode = EventNode.tag(
        "modeHandler_play", EventFilter.ENTITY,
        TagStore.tag(StorePlayerState::class), StorePlayerState.Companion::usingPlay
    )
    override fun init() {
        eventNode.addListener(PlayerBlockPlaceEvent::class.java) { it.isCancelled = true }
        eventNode.addListener(PlayerBlockBreakEvent::class.java) { it.isCancelled = true }
    }
}

private object BuildMode : ModeHandler {
    override val eventNode = EventNode.tag(
        "modeHandler_build", EventFilter.PLAYER,
        TagStore.tag(StorePlayerState::class), StorePlayerState.Companion::usingBuild
    )
    override fun init() {}
}

private object DevMode : ModeHandler {
    override val eventNode = EventNode.tag(
        "modeHandler_dev", EventFilter.PLAYER,
        TagStore.tag(StorePlayerState::class), StorePlayerState.Companion::usingDev
    )
    override fun init() {
        eventNode.addListener(PlayerBlockPlaceEvent::class.java) { try { placeBlock(it) } catch (e: Exception) {
            it.player.sendMessage("ERROR: ${e.message}")
            it.isCancelled = true
        } }
        eventNode.addListener(PlayerBlockBreakEvent::class.java) { try { breakBlock(it) } catch (e: Exception) {
            it.player.sendMessage("ERROR: ${e.message}")
            it.isCancelled = true
        } }
        initDevEvents(eventNode)
    }

    private fun placeBlock(event: PlayerBlockPlaceEvent) {
        TODO("Block placement & world code")
    }

    private fun breakBlock(event: PlayerBlockBreakEvent) {
        TODO("Block placement & world code")
    }

    private fun move(instance: Instance, start: Point, end: Point, amount: Int?) {
        TODO("Block placement & world code")
        // FORWARD & BACKWARD are in the Z axis
    }
}
