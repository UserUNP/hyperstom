package hyperstom.infernity.dev.world.mode

import net.minestom.server.event.EventNode

interface ModeHandler {
    fun init()
    fun getNode(): EventNode<*>
    override fun toString(): String

    enum class Mode(private val handler: ModeHandler) : ModeHandler {
        PLAY(PlayMode),
        BUILD(BuildMode),
        DEV(DevMode),
        ;

        override fun init() = handler.init()
        override fun getNode() = handler.getNode()
        override fun toString() = handler.toString()
    }
}
