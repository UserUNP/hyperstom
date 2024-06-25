@file:Suppress("UnstableApiUsage")

package userunp.hyperstom.world

import userunp.hyperstom.code.*
import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.SharedInstance
import net.minestom.server.instance.block.Block
import net.minestom.server.world.DimensionType
import userunp.hyperstom.*
import userunp.hyperstom.datastore.*
import java.io.File
import java.util.UUID

const val WORLDS_DIR = "worlds"
const val WORLDS_EXT = "hstom"
private val LOGGER = KotlinLogging.logger {}

val BUILD_SPAWN_POINT = Pos(0.0, 65.0, 0.0)
val DEV_SPAWN_POINT = Pos(0.0, 65.5,  0.0)

val worlds = mutableMapOf<UUID, WorldManager>()
fun getWorld(id: UUID) = worlds[id] ?: throw NullPointerException("No such world! $id")

fun initWorlds() {
    val dir = File(WORLDS_DIR)
    if (!dir.exists()) dir.mkdirs()
    else if (!dir.isDirectory) throw FileAlreadyExistsException(dir, reason = "$WORLDS_DIR is not a directory!")
    var containsHub = false
    for (str in dir.list()!!) {
        if (!str.endsWith(".$WORLDS_EXT")) continue
        val id = UUID.fromString(str.replace(".$WORLDS_EXT", ""))
        if (worlds.contains(id)) throw WorldIOException("Duplicate world id! $id")
        if (id == HUB_WORLD_ID) containsHub = true
        val world = WorldManager(id, readWorldArchive(id))
        val code = readWorldCode(world)
        code({ world.play }, eventLabel(EVENT_WORLD_INIT), world.runtimeInvoker)
    }
    if (!containsHub) {
        LOGGER.warn { "Couldn't find hub world. Creating." }
        val files = defaultWorldFiles(HUB_WORLD_INFO)
        writeWorldArchive(HUB_WORLD_ID, files)
        WorldManager(HUB_WORLD_ID, files)
    }
}

fun shutdownWorlds() {
    LOGGER.info { "Saving worlds.." }
    for ((id, w) in worlds) writeWorldArchive(id, w.files)
}

fun readWorldContributors(world: WorldManager) = PersistentStore(world).use { it.read(StoreWorldContributors::class) }
fun readWorldCode(world: WorldManager) = PersistentStore(world).use { it.read(StoreWorldCode::class) }

class WorldManager(val id: UUID, val files: WorldArchiveFiles) : StoreDataProvider<WorldVarData> {
    val info = files.info
    val build = InstanceContainer(
        UUID.nameUUIDFromBytes("$id.$WORLDS_EXT/$id.build".toByteArray()),
        DimensionType.OVERWORLD,
        files.build,
    )
    val dev = InstanceContainer(
        UUID.nameUUIDFromBytes("$WORLDS_EXT/$id.dev".toByteArray()),
        DimensionType.OVERWORLD,
        files.dev,
    )
    val play = SharedInstance(UUID.nameUUIDFromBytes("$WORLDS_EXT/$id.play".toByteArray()), build)

    val runtimeInvoker = RuntimeInvoker(this)

    init {
        worlds[id] = this
        initWorldModes(this, MinecraftServer.getGlobalEventHandler())

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
            dataLabel("__debug") to mutableListOf(
                Instruction(InstProperties.DEBUG_FRAME, EMPTY_ARGS),
            ),
            dataLabel("test") to mutableListOf(
                Instruction(InstProperties.SEND_MESSAGE, mutableMapOf(
                    "msg" to CodeVal(VAL_TYPE_TXT, TxtVal(
                        MM.deserialize("<gradient:blue:aqua:blue>Can also call functions (jump to labels)")
                    )),
                ), TARGET_DEFAULT),
                Instruction(InstProperties.ASSIGN_VAR, mutableMapOf(
                    "var" to CodeVal(VAL_TYPE_VAR, StrVal("test")),
                    "val" to CodeVal(VAL_TYPE_TXT, TxtVal(
                        MM.deserialize("<gradient:yellow:gold:yellow>And assign & resolve variables (at runtime)")
                    )),
                )),
                Instruction(InstProperties.SEND_MESSAGE, mutableMapOf(
                    "msg" to CodeVal(VAL_TYPE_VAR, StrVal("test")),
                ), TARGET_DEFAULT),
            ),
            eventLabel(EVENT_PLAYER_CHAT) to mutableListOf(
                Instruction(InstProperties.SEND_MESSAGE, mutableMapOf(
                    "msg" to CodeVal(VAL_TYPE_TXT, TxtVal(
                        MM.deserialize("<gradient:red:dark_red:red>Can target & send messages now!")
                    )),
                ), TARGET_DEFAULT),
                Instruction(InstProperties.CALL_FUNCTION, mutableMapOf(
                    "func" to CodeVal(VAL_TYPE_FUNC, dataLabel("test")),
                )),
            ),
        ))) }
    }

    override fun data() = files.varData
}
