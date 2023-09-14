package hyperstom.infernity.dev

import hyperstom.infernity.dev.command.*
import hyperstom.infernity.dev.world.mode.ModeHandler
import hyperstom.infernity.dev.tagstore.StoreWorldState
import hyperstom.infernity.dev.tagstore.TagStore
import hyperstom.infernity.dev.world.WorldManager
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.instance.block.Block
import net.minestom.server.tag.Tag
import net.minestom.server.utils.validate.Check
import net.minestom.server.world.DimensionType

const val HUB_WORLD_ID = 0
const val WORLDS_DIR = "worlds"
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

    val cmdManager = MinecraftServer.getCommandManager()
    cmdManager.register(AboutCommand())
    cmdManager.register(WorldCommand())
    cmdManager.register(PlayCommand())
    cmdManager.register(BuildCommand())
    cmdManager.register(DevCommand())

    WorldManager.initWorlds()

    eventHandler.addListener(PlayerLoginEvent::class.java) {
        TagStore(it.player).write(StoreWorldState(ModeHandler.Mode.PLAY.ordinal, HUB_WORLD_ID))
        val world = WorldManager.worlds[HUB_WORLD_ID]!!
        Check.notNull(world, "Hub world (id $HUB_WORLD_ID) does not exist!")
        world.setDefaultInstance(it)
    }
}
