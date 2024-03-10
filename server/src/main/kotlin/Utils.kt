@file:OptIn(ExperimentalSerializationApi::class)

package dev.bedcrab.hyperstom

import dev.bedcrab.hyperstom.code.CONNECTOR_MS_BLOCK
import dev.bedcrab.hyperstom.code.CodeBlock
import dev.bedcrab.hyperstom.code.isCloseBracket
import dev.bedcrab.hyperstom.code.isOpenBracket
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.serializer
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.Block.Getter
import net.minestom.server.tag.Tag
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.NBTCompound

val GSON_SERIALIZER = GsonComponentSerializer.gson()
val PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText() // TODO: idk why i have this actually
val POLAR_BUILD_TEMPLATE = getResource("BUILD")

val UP_VEC = Vec(0.0, 1.0, 0.0)
val LEFT_VEC = Vec(-1.0, 0.0, 0.0)
val RIGHT_VEC = Vec(-1.0, 0.0, 0.0)
val FORWARD_VEC = Vec(0.0, 0.0, 1.0)
val BACKWARD_VEC = Vec(0.0, 0.0, -1.0)

fun getResource(path: String) = object {}::class.java.classLoader.getResourceAsStream(path) ?: throw NullPointerException("\"$path\" does not exist as a resource")

fun <T : Any> cborByteArray(obj: T) = Cbor.encodeToByteArray(serializer(obj::class.java), obj)
@Suppress("UNCHECKED_CAST")
fun <T : Any> cborReadByteArray(type: Class<T>, bytes: ByteArray): T = Cbor.decodeFromByteArray(serializer(type), bytes) as T
inline fun <reified T> cborSerialize(obj: T, nbt: NBTCompound?): NBTCompound {
    if (nbt == null) throw NullPointerException("Failed to get nbt!")
    return nbt.toMutableCompound().set("d", NBT.String(Cbor.encodeToHexString<T>(obj))).toCompound()
}
inline fun <reified T> cborDeserialize(nbt: NBT): T {
    assert(nbt is NBTCompound) { "Not an NBT compound!" }
    val d = Tag.String("d").read(nbt as NBTCompound) ?: throw NullPointerException("No data field!")
    return Cbor.decodeFromHexString<T>(d)
}

fun shiftPoint(point: Point, dir: Vec, length: Double = 1.0): Point {
    val norm = dir.normalize()
    return point.add(norm.x * length, norm.y * length, norm.z * length)
}

fun findEndBracket(start: Point, getter: Getter): Point? {
    var nestedCounter = 0
    var unchecked = 0
    var current = start
    var msBlock: Block
    do {
        current = shiftPoint(current, FORWARD_VEC)
        msBlock = getter.getBlock(current)
        if (msBlock == Block.AIR) {
            unchecked++
            continue
        }
        unchecked = 0
        if (msBlock == CONNECTOR_MS_BLOCK) continue
        else if (isOpenBracket(msBlock)) nestedCounter++
        else if (isCloseBracket(msBlock)) nestedCounter--
        else {
            val hsBlock = try { CodeBlock.from(msBlock) } catch (_: Exception) { return null }
            if (hsBlock.type.root) return null
        }
    } while (nestedCounter != -1 && unchecked <= 2)
    if (!isCloseBracket(msBlock)) return null
    return current
}

fun findRootBlock(start: Point, getter: Getter): Pair<Point, CodeBlock>? {
    var current = start
    var unchecked = 0
    var hsBlock: CodeBlock? = null
    do {
        current = shiftPoint(current, BACKWARD_VEC)
        val msBlock = getter.getBlock(current)
        if (msBlock == Block.AIR) {
            unchecked++
            continue
        }
        unchecked = 0
        hsBlock = try { CodeBlock.from(msBlock) } catch (_: Exception) { continue }
        if (hsBlock!!.type.root) break
    } while (unchecked <= 2)
    if (hsBlock == null || !hsBlock.type.root) return null
    return current to hsBlock
}
