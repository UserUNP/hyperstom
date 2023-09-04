package hyperstom.infernity.dev

import hyperstom.infernity.dev.code.action.PlayerActions
import hyperstom.infernity.dev.code.block.CodeBlock
import hyperstom.infernity.dev.code.event.PlayerEvents
import hyperstom.infernity.dev.command.AboutCommand
import hyperstom.infernity.dev.command.BuildCommand
import hyperstom.infernity.dev.command.DevCommand
import hyperstom.infernity.dev.command.PlayCommand
import hyperstom.infernity.dev.plot.mode.ModeHandler
import hyperstom.infernity.dev.tagstore.StorePlotState
import hyperstom.infernity.dev.tagstore.TagStore
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.instance.block.Block
import net.minestom.server.tag.Tag
import net.minestom.server.world.DimensionType

const val HUB_PLOT_ID = 0
val TAG_STORE_HOME = Tag.NBT("hyperstom")

fun main() {
    val server = MinecraftServer.init()
    MojangAuth.init()
    server.start("0.0.0.0", 25565)

    val instanceManager = MinecraftServer.getInstanceManager()
    val hubInstance = instanceManager.createInstanceContainer(DimensionType.OVERWORLD)
    hubInstance.setGenerator { unit -> unit.modifier().fillHeight(49, 50, Block.GRASS_BLOCK) }

    val eventHandler = MinecraftServer.getGlobalEventHandler()
    ModeHandler.Mode.PLAY.init()
    ModeHandler.Mode.BUILD.init()
    ModeHandler.Mode.DEV.init()
    for (mode in ModeHandler.Mode.entries) eventHandler.addChild(mode.getNode())
    eventHandler.addListener(PlayerLoginEvent::class.java) {
        TagStore(it.player).write(StorePlotState(ModeHandler.Mode.PLAY.ordinal, HUB_PLOT_ID))
    }

    CodeBlock.Properties.PLAYER_ACTION.setRegistry(PlayerActions)
    CodeBlock.Properties.PLAYER_EVENT.setRegistry(PlayerEvents)

    val cmdManager = MinecraftServer.getCommandManager()
    cmdManager.register(AboutCommand())
    cmdManager.register(PlayCommand())
    cmdManager.register(BuildCommand())
    cmdManager.register(DevCommand())
}
