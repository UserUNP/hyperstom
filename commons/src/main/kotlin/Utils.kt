@file:OptIn(ExperimentalSerializationApi::class)

package dev.bedcrab.hyperstom

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.serializer
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.NBTCompound

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
    val d = (nbt as NBTCompound).getString("d") ?: throw NullPointerException("No data field!")
    return Cbor.decodeFromHexString<T>(d)
}

fun shiftPoint(point: Point, dir: Vec, length: Double = 1.0): Point {
    val norm = dir.normalize()
    return point.add(norm.x * length, norm.y * length, norm.z * length)
}
