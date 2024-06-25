package userunp.hyperstom.code

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass
import net.kyori.adventure.text.Component
import userunp.hyperstom.Identifiable
import userunp.hyperstom.IdentifiableSerializer
import userunp.hyperstom.MM
import kotlin.reflect.KClass

private class CodeValBoxSerializer<T : CodeValBox>(private val type: CodeValType<T>) : KSerializer<T> {
    override val descriptor = PrimitiveSerialDescriptor(type.name, PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = type.deserialize(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: T) = encoder.encodeString(value.name)
}

sealed interface CodeValBox : Identifiable
data object PlaceholderVal : CodeValBox { override val name get() = TODO("placeholder!") }
data object NullVal : CodeValBox { override val name = "(null)" }
data class StrVal(override val name: String) : CodeValBox
data class NumVal(val num: Float) : CodeValBox { override val name = "$num" }
data class BoolVal(val bool: Boolean) : CodeValBox { override val name = "$bool" }
data class TxtVal(val txt: Component) : CodeValBox { override val name = MM.serialize(txt) }

val codeValPolymorphic: PolymorphicModuleBuilder<CodeValBox>.() -> Unit = {
    subclass(CodeValBoxSerializer(VAL_TYPE_NULL))
    subclass(CodeValBoxSerializer(VAL_TYPE_TYPE))
    subclass(CodeValBoxSerializer(VAL_TYPE_EVENT_VAL))
    subclass(CodeValBoxSerializer(VAL_TYPE_TARGET))
    subclass(CodeValBoxSerializer(VAL_TYPE_FUNC))

    subclass(CodeValBoxSerializer(VAL_TYPE_STR))
    subclass(CodeValBoxSerializer(VAL_TYPE_NUM))
    subclass(CodeValBoxSerializer(VAL_TYPE_BOOL))

    subclass(CodeValBoxSerializer(VAL_TYPE_TXT))
}

fun getCodeValType(name: String) = nameToCodeValType[name] ?: throw RuntimeException("Unsupported code value type! $name")
private val nameToCodeValType = mutableMapOf<String, CodeValType<*>>()
private val typeToCodeValType = mutableMapOf<KClass<*>, CodeValType<*>>()

private fun placeholder(n: String) = CodeValType(n, PlaceholderVal::class) { TODO("placeholder!") }

// reflection
val VAL_TYPE_NULL = CodeValType("NULL", NullVal::class) { NullVal }
val VAL_TYPE_TYPE = CodeValType("TYPE", CodeValType::class) { getCodeValType(it) }
val VAL_TYPE_PARAM = placeholder("PARAM")
val VAL_TYPE_VAR = CodeValType("VAR", StrVal::class, true) { StrVal(it) }
val VAL_TYPE_GLOBAL = placeholder("GLOBAL")
val VAL_TYPE_EVENT_VAL = CodeValType("EVENT_VAL", EventVal::class) { getEventVal(it) }
val VAL_TYPE_TARGET = CodeValType("TARGET", EventTarget::class) { getEventTarget(it) }
val VAL_TYPE_FUNC = CodeValType("FUNC", CodeLabel::class) { dataLabel(it) }
// primitive
val VAL_TYPE_STR = CodeValType("STR", StrVal::class) { StrVal(it) }
val VAL_TYPE_NUM = CodeValType("NUM", NumVal::class) { NumVal(it.toFloat()) }
val VAL_TYPE_BOOL = CodeValType("BOOL", BoolVal::class) { BoolVal(it.toBoolean()) }
val VAL_TYPE_LIST = placeholder("LIST")
val VAL_TYPE_EXPR = placeholder("EXPR") //TODO: evaluate vars n math and concat them to strings or used as a number
// minecraft
val VAL_TYPE_TXT = CodeValType("TXT", TxtVal::class) { TxtVal(MM.deserialize(it)) }
val VAL_TYPE_ITEM = placeholder("ITEM") //TODO: this is annoying because i have to waste storing the itemstack for mc and the item data for hyperstom
val VAL_TYPE_PARTICLE = placeholder("PARTICLE") //TODO: bunch of fields

private object CodeValueTypeSerializer : IdentifiableSerializer<CodeValType<*>>(::getCodeValType)
@Serializable(CodeValueTypeSerializer::class) data class CodeValType<T : CodeValBox>(
    override val name: String,
    val typeClass: KClass<T>,
    val runtime: Boolean = false,
    private val getter: (name: String) -> T,
) : CodeValBox {
    init {
        nameToCodeValType[name] = this
        typeToCodeValType[typeClass] = this
    }
    fun deserialize(data: String) = getter(data)
}

@Serializable data class CodeVal<T : CodeValBox>(val type: CodeValType<T>, val value: T)
val NULL_VALUE = CodeVal(VAL_TYPE_NULL, NullVal)
