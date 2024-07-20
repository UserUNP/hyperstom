@file:Suppress("UnstableApiUsage")

package ma.userunp.hyperstom.world

import io.github.oshai.kotlinlogging.KotlinLogging
import ma.userunp.hyperstom.*
import ma.userunp.hyperstom.code.*
import ma.userunp.hyperstom.code.impl.*
import net.hollowcube.polar.PolarLoader
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.SharedInstance
import net.minestom.server.world.DimensionType
import java.io.File
import java.util.*
import kotlin.time.measureTime
private val LOGGER = KotlinLogging.logger {}

val BUILD_SPAWN_POINT = Pos(0.0, 65.0, 0.0)

val worlds = mutableMapOf<UUID, WorldManager>()

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
        try {
            val world = WorldManager(id, readWorldArchive(id))
            world.files.code.invoke({ world.play }, eventLabel(CodeEvent.INIT), world.runtimeInvoker)
        } catch (e: Exception) { throw RuntimeException("Couldn't initialize world! $id", e) }
    }
    if (!containsHub) {
        LOGGER.warn { "Couldn't find hub world. Creating." }
        val files = defaultWorldFiles(HUB_WORLD_INFO)
        WorldManager(HUB_WORLD_ID, files)
        writeWorldArchive(HUB_WORLD_ID, files)
    }
}

fun shutdownWorlds() {
    LOGGER.info { "Saving worlds.." }
    for ((id, w) in worlds) writeWorldArchive(id, w.files)
}

class WorldManager(val id: UUID, val files: WorldArchiveFiles) {
    val build = InstanceContainer(
        UUID.nameUUIDFromBytes("$WORLDS_EXT/$id.build".toByteArray()),
        DimensionType.OVERWORLD,
        PolarLoader(files.build),
    )
    val play = SharedInstance(UUID.nameUUIDFromBytes("$WORLDS_EXT/$id.play".toByteArray()), build)

    val runtimeInvoker = RuntimeInvoker(id)

    init {
        //TODO: remove this shit
        val dur = measureTime {
            files.code.apply {
                set(dataLabel("__debug"), mutableListOf(
                    CodeInst(InstDebugFrame, mutableListOf()),
                ))
                set(dataLabel("test"), mutableListOf(
                    CodeInst(InstSendMessage, mutableListOf(
                        ValTypeTxt.get(txtVal("<gradient:blue:aqua:blue>Can also call functions (jump to labels)")),
                    ), TargetDefault),
                    CodeInst(InstAssignVar, mutableListOf(
                        ValTypeVar.get("testvar"),
                        ValTypeTxt.get(txtVal("<gradient:yellow:gold:yellow>And assign & resolve variables (at runtime)</gradient> you sent the message: <gradient:dark_green:green:dark_green>")),
                    )),
                    CodeInst(InstToTxt, mutableListOf(
                        ValTypeVar.get("testvar"),
                        ValTypeVar.get("testvar"),
                        ValTypeEventVal.get(EventValChatMsg),
                    ), TargetDefault),
                    CodeInst(InstSendMessage, mutableListOf(
                        ValTypeVar.get("testvar"),
                    ), TargetDefault),
                ))
                set(eventLabel(CodeEvent.CHAT), mutableListOf(
                    CodeInst(InstSendMessage, mutableListOf(
                        ValTypeTxt.get(txtVal("<gradient:red:dark_red:red>Can target & send messages now!")),
                    ), TargetDefault),
                    CodeInst(InstCallFunction, mutableListOf(
                        ValTypeLabel.get(dataLabel("test")),
                    )),
                ))
            }

            val instanceManager = MinecraftServer.getInstanceManager()
            instanceManager.registerInstance(build)
            instanceManager.registerSharedInstance(play)

            worlds[id] = this
            initWorldModes(this, MinecraftServer.getGlobalEventHandler())
        }
        LOGGER.info { "Loaded world $id - $dur" }
    }
}
