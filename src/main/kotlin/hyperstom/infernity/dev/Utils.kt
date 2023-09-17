package hyperstom.infernity.dev

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.Direction

val gsonSerializer = GsonComponentSerializer.gson()
val plainSerializer = PlainTextComponentSerializer.plainText()

object Utils {
    fun getResource(path: String) = object {}::class.java.classLoader.getResourceAsStream(path) ?: throw NullPointerException("\"$path\" does not exist as a resource")

    fun shiftPoint(point: Point, dir: Direction, length: Double = 1.0) = point.add(dir.normalX().toDouble() * length, dir.normalY().toDouble() * length, dir.normalZ().toDouble() * length)

    fun findEndPiston(start: Point, getter: Block.Getter, sticky: Boolean = false): Point? {
        val pistonBlock = (if (sticky) Block.STICKY_PISTON else Block.PISTON).name()
        val uncheckedLimit = 100
        var nestedCounter = 0
        var unchecked = 0
        var current = start
        do {
            current = shiftPoint(current, Direction.SOUTH)
            val block = getter.getBlock(current)
            if (block == Block.AIR) {
                unchecked++
                continue
            }
            unchecked = 0
            if (block.name() != pistonBlock) continue
            if (block.getProperty("facing") == "south") nestedCounter++
            else if (block.getProperty("facing") == "north") nestedCounter--
        } while (nestedCounter != -1 && unchecked < uncheckedLimit)
        if (getter.getBlock(current).name() != pistonBlock) return null
        return current
    }
}

