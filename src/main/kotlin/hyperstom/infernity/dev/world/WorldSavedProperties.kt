package hyperstom.infernity.dev.world

import net.minestom.server.coordinate.Pos
import net.minestom.server.network.NetworkBuffer
import java.nio.ByteBuffer
import java.util.Hashtable
import java.util.UUID

// TODO: saved vars file
@Suppress("UnstableApiUsage")
/**
 * Read/write in order: owner, name, spawnLoc, contributors
 */
data class WorldSavedProperties(val name: String, val owner: UUID?, val spawnLoc: Pos?, val contributors: Map<UUID, ContributorLevel>) {
    fun getBytes(): ByteArray = NetworkBuffer.makeArray {
        it.write(NetworkBuffer.STRING, name)
        it.writeOptional(NetworkBuffer.OPT_UUID, owner)
        it.writeOptional(NetworkBuffer.OPT_BLOCK_POSITION, spawnLoc)
        it.write(NetworkBuffer.INT, contributors.size)
        for (contrib in contributors) {
            it.write(NetworkBuffer.UUID, contrib.key)
            it.writeEnum(ContributorLevel::class.java, contrib.value)
        }
    }

    companion object {
        fun read(file: ByteArray): WorldSavedProperties {
            val buffer = NetworkBuffer(ByteBuffer.wrap(file), false)
            val name = buffer.read(NetworkBuffer.STRING)
            val owner = buffer.readOptional(NetworkBuffer.OPT_UUID)
            val spawnLoc = buffer.readOptional(NetworkBuffer.OPT_BLOCK_POSITION)
            val contributors = Hashtable<UUID, ContributorLevel>()
            val contributorsLength = buffer.read(NetworkBuffer.INT)
            for (i in 0..<contributorsLength) contributors[buffer.read(NetworkBuffer.UUID)] = buffer.readEnum(ContributorLevel::class.java)
            return WorldSavedProperties(name, owner, spawnLoc?.let { Pos(it) }, contributors)
        }
    }
}
