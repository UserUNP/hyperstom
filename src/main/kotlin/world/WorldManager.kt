@file:Suppress("UnstableApiUsage")

package userunp.hyperstom.world

import userunp.hyperstom.datastore.PersistentStore
import userunp.hyperstom.datastore.StoreDataProvider
import userunp.hyperstom.code.*
import userunp.hyperstom.datastore.StoreWorldCode
import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.SharedInstance
import net.minestom.server.instance.block.Block
import net.minestom.server.world.DimensionType
import userunp.hyperstom.HUB_WORLD_ID
import userunp.hyperstom.HUB_WORLD_INFO
import userunp.hyperstom.initWorldModes
import java.io.File
import java.util.UUID

const val WORLDS_DIR = "worlds"
const val WORLDS_EXT = "hstom"
private val LOGGER = KotlinLogging.logger {}

val BUILD_SPAWN_POINT = Pos(0.0, 65.0, 0.0)
val DEV_SPAWN_POINT = Pos(0.0, 65.5,  0.0)

val worlds = mutableMapOf<UUID, WorldManager>()
fun getWorld(id: UUID) = worlds[id] ?: throw NullPointerException("World with id $id does not exist!")

fun initWorlds(eventHandler: EventNode<Event>) {
    val dir = File(WORLDS_DIR)
    if (!dir.exists()) dir.mkdirs()
    else if (!dir.isDirectory) throw FileAlreadyExistsException(dir, null, "$WORLDS_DIR is not a directory!")
    var containsHub = false
    for (str in dir.list()!!) {
        if (!str.endsWith(".$WORLDS_EXT")) continue
        val id = UUID.fromString(str.replace(".$WORLDS_EXT", ""))
        if (worlds.contains(id)) throw RuntimeException("Duplicate world id! $id")
        if (id == HUB_WORLD_ID) containsHub = true
        val world = WorldManager(id, readWorldArchive(id))
        worlds[id] = world
        initWorldModes(world, eventHandler)
        val code = readWorldCode(world)
        code({ world.play }, eventLabel(EVENT_WORLD_INIT), world)
    }
    if (!containsHub) {
        LOGGER.warn { "Couldn't find hub world. Creating." }
        val files = defaultWorldFiles(HUB_WORLD_INFO)
        writeWorldArchive(HUB_WORLD_ID, files)
        worlds[HUB_WORLD_ID] = WorldManager(HUB_WORLD_ID, files)
    }
}

fun shutdownWorlds() {
    LOGGER.info { "Saving worlds.." }
    for ((id, w) in worlds) writeWorldArchive(id, w.files)
}

fun readWorldCode(world: WorldManager) = PersistentStore(world).use { it.read(StoreWorldCode::class) }

class WorldManager(val id: UUID, val files: WorldArchiveFiles) : StoreDataProvider<WorldVarData> {
    val info = files.info
    val build = InstanceContainer(
        UUID.nameUUIDFromBytes("$id.$WORLDS_EXT/$id.build".toByteArray()),
        DimensionType.OVERWORLD,
        files.build
    )
    val dev = InstanceContainer(
        UUID.nameUUIDFromBytes("$WORLDS_EXT/$id.dev".toByteArray()),
        DimensionType.OVERWORLD,
        files.dev
    )
    val play = SharedInstance(UUID.nameUUIDFromBytes("$WORLDS_EXT/$id.play".toByteArray()), build)

    val runtimeInvoker = RuntimeInvoker(this)

    init {
        val instanceManager = MinecraftServer.getInstanceManager()
        dev.setGenerator { unit ->
            val modifier = unit.modifier()
            modifier.fillHeight(64, 65, Block.GRASS_BLOCK)
            modifier.fillHeight(58, 63, Block.STONE)
        }
        instanceManager.registerInstance(build)
        instanceManager.registerInstance(dev)
        instanceManager.registerSharedInstance(play)
        LOGGER.info { "Registered world $id." }

        //TODO: remove this shit
        PersistentStore(this).use { it.write(StoreWorldCode(mutableMapOf(
            dataLabel("can be anything lol") to mutableListOf(
                Instruction(InstProperties.PRINT_INSTRUCTIONS, EMPTY_ARGS),
                Instruction(InstProperties.DEBUG_INST, Arguments(
                    "first" to mutableListOf(CodeValue(VALUE_TYPE_STR, StrVal("yeepee"))),
                    "second" to mutableListOf(CodeValue(VALUE_TYPE_BOOL, BoolVal(true))),
                ))
            ),
            eventLabel(EVENT_PLAYER_CHAT) to mutableListOf(
                Instruction(InstProperties.PRINT_INSTRUCTIONS, EMPTY_ARGS),
            )
        ))) }
    }

    override fun data() = files.varData
}
