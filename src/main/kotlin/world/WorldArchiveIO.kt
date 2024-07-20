@file:Suppress("UnstableApiUsage")

package ma.userunp.hyperstom.world

import io.github.oshai.kotlinlogging.KotlinLogging
import ma.userunp.hyperstom.WorldIOException
import ma.userunp.hyperstom.code.InstLabelMap
import ma.userunp.hyperstom.code.ValTypeCInst
import ma.userunp.hyperstom.code.ValTypeLabel
import ma.userunp.hyperstom.code.ValTypeStr
import ma.userunp.hyperstom.getResource
import net.hollowcube.polar.PolarReader
import net.hollowcube.polar.PolarWorld
import net.hollowcube.polar.PolarWriter
import net.minestom.server.coordinate.Pos
import net.minestom.server.network.NetworkBuffer
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.util.*
import java.util.zip.Deflater
import kotlin.io.path.Path
import kotlin.time.measureTime

private val LOGGER = KotlinLogging.logger {}
private val DEFAULT_BUILD = PolarReader.read(getResource("BUILD.compressed").readBytes())
const val WORLDS_DIR = "worlds"
const val WORLDS_EXT = "hstom"
private const val WORLDS_MAGIC = 0x48595052

private fun getIn(id: UUID) = ZstdCompressorInputStream(Files.newInputStream(Path(WORLDS_DIR, "$id.$WORLDS_EXT"))).buffered()
private fun getOut(id: UUID) = ZstdCompressorOutputStream(Files.newOutputStream(Path(WORLDS_DIR, "$id.$WORLDS_EXT")), Deflater.BEST_COMPRESSION).buffered()

class WorldArchiveFiles(
    val info: WorldInfo,
    val contrib: WorldContrib,
    val code: WorldCode,
    val build: PolarWorld,
)

fun defaultWorldFiles(i: WorldInfo) = WorldArchiveFiles(
    i, WorldContrib(mutableMapOf()), WorldCode(linkedMapOf()), DEFAULT_BUILD
)

private interface ArchiveFileType<T : Any> : NetworkBuffer.Type<T>

private object FileTypeInfo : ArchiveFileType<WorldInfo> {
    override fun write(buffer: NetworkBuffer, value: WorldInfo) {
        NetworkBuffer.STRING.write(buffer, value.title)
        NetworkBuffer.OPT_UUID.write(buffer, value.owner)
        NetworkBuffer.OPT_BLOCK_POSITION.write(buffer, value.spawnLoc)
    }
    override fun read(buffer: NetworkBuffer) = WorldInfo(
        NetworkBuffer.STRING.read(buffer),
        NetworkBuffer.OPT_UUID.read(buffer),
        NetworkBuffer.OPT_BLOCK_POSITION.read(buffer)?.let { Pos(it) } ?: BUILD_SPAWN_POINT,
    )
}

private object FileTypeContrib : ArchiveFileType<WorldContrib> {
    override fun write(buffer: NetworkBuffer, value: WorldContrib) {
        buffer.writeMap(ValTypeStr, NetworkBuffer.Enum(ContribLevel::class.java), value.map)
    }
    override fun read(buffer: NetworkBuffer) = WorldContrib(buffer.readMap(
        NetworkBuffer.STRING, NetworkBuffer.Enum(ContribLevel::class.java), 20,
    ))
}

private object FileTypeCode : ArchiveFileType<WorldCode> {
    override fun write(buffer: NetworkBuffer, value: WorldCode) {
        val labels = value.getLabels()
        buffer.writeCollection(ValTypeLabel, labels)
        for (l in labels) buffer.writeCollection(ValTypeCInst, value.resolveLabel(l))
    }
    override fun read(buffer: NetworkBuffer): WorldCode {
        val labels = buffer.readCollection(ValTypeLabel, Int.MAX_VALUE)
        val labelsMap: InstLabelMap = LinkedHashMap(labels.size)
        for (l in labels) labelsMap[l] = buffer.readCollection(ValTypeCInst, Int.MAX_VALUE)
        return WorldCode(labelsMap)
    }
}

private object FileTypeBuild : ArchiveFileType<PolarWorld> {
    override fun write(buffer: NetworkBuffer, value: PolarWorld) = NetworkBuffer.BYTE_ARRAY.write(buffer,
        PolarWriter.write(value.apply { setCompression(PolarWorld.CompressionType.NONE) })
    )
    override fun read(buffer: NetworkBuffer) = PolarReader.read(NetworkBuffer.BYTE_ARRAY.read(buffer))
}

private object FileTypeWorld : ArchiveFileType<WorldArchiveFiles> {
    override fun write(buffer: NetworkBuffer, value: WorldArchiveFiles) {
        NetworkBuffer.INT.write(buffer, WORLDS_MAGIC)
        FileTypeInfo.write(buffer, value.info)
        FileTypeContrib.write(buffer, value.contrib)
        FileTypeCode.write(buffer, value.code)
        FileTypeBuild.write(buffer, value.build)
    }
    override fun read(buffer: NetworkBuffer): WorldArchiveFiles {
        if (NetworkBuffer.INT.read(buffer) != WORLDS_MAGIC) throw WorldIOException("Not a hyperstom world!")
        return WorldArchiveFiles(
            FileTypeInfo.read(buffer),
            FileTypeContrib.read(buffer),
            FileTypeCode.read(buffer),
            FileTypeBuild.read(buffer),
        )
    }
}

fun readWorldArchive(id: UUID) = FileTypeWorld.read(NetworkBuffer(ByteBuffer.wrap(getIn(id).use { it.readAllBytes() })))

fun writeWorldArchive(id: UUID, files: WorldArchiveFiles) = getOut(id).use {
    val dur = measureTime { it.write(NetworkBuffer.makeArray { FileTypeWorld.write(it, files) }) }
    LOGGER.info { "Saved world archive $id - $dur" }
}
