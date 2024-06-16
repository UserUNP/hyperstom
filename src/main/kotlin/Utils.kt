@file:OptIn(ExperimentalSerializationApi::class)

package userunp.hyperstom

import kotlinx.serialization.*
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import net.kyori.adventure.text.minimessage.MiniMessage
import userunp.hyperstom.code.CodeValBox
import userunp.hyperstom.code.codeValPolymorphic

/// cbor serialization stuff

private val module = SerializersModule {
    polymorphic(CodeValBox::class, builderAction = codeValPolymorphic)
}

private val cbor = Cbor { serializersModule = module }

fun <T : Any> cborByteArray(obj: T) = cbor.encodeToByteArray(serializer(obj::class.java), obj)
@Suppress("UNCHECKED_CAST")
fun <T : Any> cborReadByteArray(type: Class<T>, bytes: ByteArray): T = cbor.decodeFromByteArray(serializer(type), bytes) as T

interface Identifiable { val name: String }
abstract class IdentifiableSerializer<T : Identifiable>(protected val get: (name: String) -> T) : KSerializer<T> {
    override val descriptor = PrimitiveSerialDescriptor("Identifiable", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = get(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: T) = encoder.encodeString(value.name)
}

/// misc

val MM = MiniMessage.miniMessage()

fun getResource(path: String) = object {}::class.java.classLoader.getResourceAsStream(path)
    ?: throw NullPointerException("\"$path\" does not exist as a resource")

fun bytesToHumanReadable(bytes: Int) = when {
    bytes >= 1 shl 30 -> "%.2f GB".format(bytes.toFloat() / (1 shl 30))
    bytes >= 1 shl 20 -> "%.2f MB".format(bytes.toFloat() / (1 shl 20))
    bytes >= 1 shl 10 -> "%.2f kB".format(bytes.toFloat() / (1 shl 10))
    else -> "$bytes bytes"
}
