package hyperstom.infernity.dev.world

import hyperstom.infernity.dev.WORLDS_DIR
import net.hollowcube.polar.PolarLoader
import net.hollowcube.polar.PolarWriter
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.compress.utils.IOUtils
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.FileOutputStream

class WorldArchiveIO(val id: Int) {
    fun read(): Data {
        val stream = TarArchiveInputStream(GzipCompressorInputStream(FileInputStream("$WORLDS_DIR/$id.tar.gz")))
        var entry: ArchiveEntry? = stream.nextEntry
        lateinit var propsFile: ByteArray
        lateinit var buildFile: ByteArray
        lateinit var devFile: ByteArray
        while (entry != null) {
            if (entry.isDirectory || !stream.canReadEntryData(entry)) continue
            when(entry.name) {
                "properties" -> propsFile = stream.readBytes()
                "build" -> buildFile = stream.readBytes()
                "dev" -> devFile = stream.readBytes()
            }
            entry = stream.nextEntry
        }
        val properties = WorldSavedProperties.read(propsFile)
        val build = PolarLoader(ByteArrayInputStream(buildFile))
        val dev = PolarLoader(ByteArrayInputStream(devFile))
        return Data(properties, build, dev)
    }

    fun write(archive: Data) {
        val stream = TarArchiveOutputStream(GzipCompressorOutputStream(FileOutputStream("$WORLDS_DIR/$id.tar.gz")))

        val propsEntry = TarArchiveEntry("properties")
        val propsFile = archive.propsFile()
        propsEntry.size = propsFile.available().toLong()
        stream.putArchiveEntry(propsEntry)
        IOUtils.copy(propsFile, stream)
        stream.closeArchiveEntry()

        val buildEntry = TarArchiveEntry("build")
        val buildFile = archive.buildFile()
        buildEntry.size = buildFile.available().toLong()
        stream.putArchiveEntry(buildEntry)
        IOUtils.copy(buildFile, stream)
        stream.closeArchiveEntry()

        val devEntry = TarArchiveEntry("dev")
        val devFile = archive.devFile()
        devEntry.size = devFile.available().toLong()
        stream.putArchiveEntry(devEntry)
        IOUtils.copy(devFile, stream)
        stream.closeArchiveEntry()

        stream.flush()
        stream.close()
    }

    data class Data(val properties: WorldSavedProperties, val build: PolarLoader, val dev: PolarLoader) {
        fun propsFile() = ByteArrayInputStream(properties.getBytes())
        fun buildFile() = ByteArrayInputStream(PolarWriter.write(build.world()))
        fun devFile() = ByteArrayInputStream(PolarWriter.write(dev.world()))
    }
}
