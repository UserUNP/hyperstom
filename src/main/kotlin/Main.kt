package dev.bedcrab.hyperstom

import dev.bedcrab.hyperstom.world.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.util.UUID

private const val VERSION = "1.0.0-alpha"
private val LOGGER = KotlinLogging.logger {}

fun main() {
    LOGGER.info { "\t*** Hyperstom - v$VERSION ***" }
    LOGGER.info { "\t> Starting MC server..." }
    startMinecraftServer()
    LOGGER.info { "\t> Initializing worlds..." }
    initWorlds()
}

fun initWorlds() {
    val dir = File(WORLDS_DIR)
    if (!dir.exists()) dir.mkdirs()
    else if (!dir.isDirectory) throw FileAlreadyExistsException(dir, null, "$WORLDS_DIR is not a directory!")
    for (str in dir.list()!!) {
        if (!str.endsWith(".$WORLDS_EXT")) continue
        val id = UUID.fromString(str.replace(".$WORLDS_EXT", ""))
        if (worlds.contains(id)) throw RuntimeException("Duplicate world id ($id)!")
        worlds[id] = WorldManager(id, readWorldArchive(id))
    }
    if (!worlds.containsKey(HUB_WORLD_ID)) {
        LOGGER.warn { "Couldn't find hub world. Creating." }
        val files = WorldArchiveFiles.default(HUB_WORLD_INFO)
        writeWorldArchive(HUB_WORLD_ID, files)
        worlds[HUB_WORLD_ID] = WorldManager(HUB_WORLD_ID, files)
    }
}
