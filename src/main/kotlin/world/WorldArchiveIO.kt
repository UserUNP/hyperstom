@file:Suppress("UnstableApiUsage")

package userunp.hyperstom.world

import userunp.hyperstom.datastore.PersistentData
import userunp.hyperstom.getResource
import net.hollowcube.polar.PolarLoader
import net.hollowcube.polar.PolarReader
import net.hollowcube.polar.PolarWorld
import net.hollowcube.polar.PolarWriter
import net.minestom.server.coordinate.Pos
import net.minestom.server.network.NetworkBuffer
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.UUID

private const val VAR_DATA_PREFIX = "DATA/"

private typealias ArchiveOutputStream = TarArchiveOutputStream
private typealias ArchiveInputStream = TarArchiveInputStream
private typealias ArchiveEntry = TarArchiveEntry

private fun getInputStream(id: UUID) = TarArchiveInputStream(GzipCompressorInputStream(FileInputStream("$WORLDS_DIR/$id.$WORLDS_EXT")))
private fun getOutputStream(id: UUID) = TarArchiveOutputStream(GzipCompressorOutputStream(FileOutputStream("$WORLDS_DIR/$id.$WORLDS_EXT")))
private fun saveEntry(stream: ArchiveOutputStream, name: String, file: ByteArrayInputStream) {
    val entry = TarArchiveEntry(name)
    entry.size = file.available().toLong()
    stream.putArchiveEntry(entry)
    IOUtils.copy(file, stream)
    stream.closeArchiveEntry()
}

fun readWorldArchive(id: UUID): WorldArchiveFiles {
    val stream = getInputStream(id)
    val map = mutableMapOf<WorldFileType, ByteArray>()
    val varData = WorldVarData(mutableMapOf())
    stream.use {
        var entry = stream.nextEntry
        while (entry != null) {
            if (!stream.canReadEntryData(entry)) continue
            if (entry.name.startsWith(VAR_DATA_PREFIX)) readVarData(entry, stream, varData)
            else try { map[WorldFileType.valueOf(entry.name)] = stream.readBytes() } catch (_: Exception) {}
            entry = stream.nextEntry
        }
        checkWorldFiles(id, map)
        return WorldArchiveFiles(
            readWorldInfo(map[WorldFileType.INFO]!!), varData,
            PolarLoader(PolarReader.read(map[WorldFileType.BUILD]!!)),
            PolarLoader(PolarReader.read(map[WorldFileType.DEV]!!))
        )
    }
}

fun writeWorldArchive(id: UUID, files: WorldArchiveFiles) = getOutputStream(id).use {
    WorldFileType.INFO.save(files.infoFile(), it)
    WorldFileType.BUILD.save(files.buildFile(), it)
    WorldFileType.DEV.save(files.devFile(), it)
    WorldFileType.saveVarData(files.varDataFiles(), it)
    it.flush()
}

private enum class WorldFileType {
    INFO, BUILD, DEV;
    fun save(file: ByteArrayInputStream, stream: ArchiveOutputStream) = saveEntry(stream, name, file)
    companion object {
        fun saveVarData(map: Map<String, ByteArrayInputStream>, stream: ArchiveOutputStream) {
            for ((name, file) in map) saveEntry(stream, name, file)
        }
    }
}

private fun checkWorldFiles(id: UUID, map: Map<WorldFileType, ByteArray>) {
    for (type in WorldFileType.entries) {
        if (map[type] == null) throw RuntimeException("World $id is missing the ${type.name} file!")
    }
}

class WorldArchiveFiles(
    val info: WorldInfo, val varData: WorldVarData,
    val build: PolarLoader, val dev: PolarLoader,
) {
    fun infoFile() = ByteArrayInputStream(info.getBytes())
    fun buildFile() = ByteArrayInputStream(PolarWriter.write(build.world()))
    fun devFile() = ByteArrayInputStream(PolarWriter.write(dev.world()))
    fun varDataFiles(): MutableMap<String, ByteArrayInputStream> {
        val map = mutableMapOf<String, ByteArrayInputStream>()
        for ((section, data) in varData.map) map["$VAR_DATA_PREFIX$section"] = ByteArrayInputStream(data)
        return map
    }
}

fun defaultWorldFiles(i: WorldInfo) = WorldArchiveFiles(
    i, WorldVarData(mutableMapOf()),
    PolarLoader(getResource("BUILD")),
    PolarLoader(PolarWorld().apply { setCompression(PolarWorld.CompressionType.NONE) })
)

/**
 * Read/write in order:
 *  - name
 *  - owner (optional)
 *  - spawnLoc (optional)
 */
data class WorldInfo(val name: String, val owner: UUID?, val spawnLoc: Pos?) {
    fun getBytes(): ByteArray = NetworkBuffer.makeArray {
        it.write(NetworkBuffer.STRING, name)
        it.writeOptional(NetworkBuffer.OPT_UUID, owner)
        it.writeOptional(NetworkBuffer.OPT_BLOCK_POSITION, spawnLoc)
    }
}

private fun readWorldInfo(file: ByteArray): WorldInfo {
    // minestom network buffer
    val msBuffer = NetworkBuffer(ByteBuffer.wrap(file), false)
    val name = msBuffer.read(NetworkBuffer.STRING)
    val owner = msBuffer.readOptional(NetworkBuffer.OPT_UUID)
    val spawnLoc = msBuffer.readOptional(NetworkBuffer.OPT_BLOCK_POSITION)
    return WorldInfo(name, owner, spawnLoc?.let { Pos(it) })
}

/**
 * Variably sized world data
 */
class WorldVarData(val map: MutableMap<String, ByteArray>) : PersistentData {
    override operator fun get(section: String) = map[section] ?: throw NullPointerException("Section with name $section does not exist!")
    override operator fun set(section: String, bytes: ByteArray) {
        map[section] = bytes
    }
}

private fun readVarData(entry: ArchiveEntry, stream: ArchiveInputStream, varData: WorldVarData) {
    val bytes = stream.readBytes()
    varData[entry.name.removePrefix(VAR_DATA_PREFIX)] = bytes
}

enum class ContributorLevel {
    ADMIN, DEVELOPER, BUILDER;
    fun hasPerm(level: ContributorLevel) = level.ordinal >= ordinal
}
