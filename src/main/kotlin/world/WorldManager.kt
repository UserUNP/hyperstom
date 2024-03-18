@file:Suppress("UnstableApiUsage")

package dev.bedcrab.hyperstom.world

import dev.bedcrab.hyperstom.datastore.PersistentStore
import dev.bedcrab.hyperstom.datastore.StoreDataProvider
import dev.bedcrab.hyperstom.code.*
import dev.bedcrab.hyperstom.datastore.StoreWorldCode
import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.SharedInstance
import net.minestom.server.instance.block.Block
import net.minestom.server.world.DimensionType
import java.util.UUID

const val WORLDS_DIR = "worlds"
const val WORLDS_EXT = "hstom"
private val LOGGER = KotlinLogging.logger {}

val BUILD_SPAWN_POINT = Pos(0.0, 65.0, 0.0)
val DEV_SPAWN_POINT = Pos(0.0, 65.5,  0.0)

val worlds = mutableMapOf<UUID, WorldManager>()
fun getWorld(id: UUID) = worlds[id] ?: throw NullPointerException("World with id $id does not exist!")

class WorldManager(val id: UUID, val files: WorldArchiveFiles) : StoreDataProvider<WorldVarData> {
    val info = files.info
    val build = InstanceContainer(UUID.nameUUIDFromBytes("$id.$WORLDS_EXT/$id.build".toByteArray()), DimensionType.OVERWORLD, files.build)
    val dev = InstanceContainer(UUID.nameUUIDFromBytes("$WORLDS_EXT/$id.dev".toByteArray()), DimensionType.OVERWORLD, files.dev)
    val play = SharedInstance(UUID.nameUUIDFromBytes("$WORLDS_EXT/$id.play".toByteArray()), build)
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

        val testInstList = mutableListOf(Instruction(InstProperties.PRINT_INSTRUCTIONS))
        val testTypeEntry = rootCodeBlockEntry(EVENT_BLOCK_TYPE, HSEvent.WORLD_INITIALIZATION)
        PersistentStore(this).use {
            it.write(
                StoreWorldCode(mutableMapOf(
                testTypeEntry to testInstList
            ))
            )
        }
    }
    override fun data() = files.varData
}
