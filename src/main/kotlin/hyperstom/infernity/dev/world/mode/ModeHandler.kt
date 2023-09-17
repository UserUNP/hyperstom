package hyperstom.infernity.dev.world.mode

import net.minestom.server.event.EventNode

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
