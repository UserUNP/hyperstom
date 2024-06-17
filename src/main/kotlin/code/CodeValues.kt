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

private class CodeValBoxSerializer<T : CodeValBox>(private val type: CodeValueType<T>) : KSerializer<T> {
    override val descriptor = PrimitiveSerialDescriptor(type.name, PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = type.deserialize(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: T) = encoder.encodeString(value.name)
}

sealed interface CodeValBox : Identifiable
data object PlaceholderVal : CodeValBox { override val name get() = TODO("placeholder!") }
data object NullVal : CodeValBox { override val name = "(null)" }
data class StrVal(val str: String) : CodeValBox { override val name = str }
data class NumVal(val num: Float) : CodeValBox { override val name = "$num" }
data class BoolVal(val bool: Boolean) : CodeValBox { override val name = "$bool" }
data class TxtVal(val text: Component) : CodeValBox { override val name = MM.serialize(text) }

val codeValPolymorphic: PolymorphicModuleBuilder<CodeValBox>.() -> Unit = {
    subclass(CodeValBoxSerializer(VALUE_TYPE_NULL))
    subclass(CodeValBoxSerializer(VALUE_TYPE_STR))
    subclass(CodeValBoxSerializer(VALUE_TYPE_BOOL))
    subclass(CodeValBoxSerializer(VALUE_TYPE_TXT))
}

fun getCodeValueType(name: String) = nameToCodeValueType[name] ?: throw RuntimeException("Unsupported code value type! $name")
private val nameToCodeValueType = mutableMapOf<String, CodeValueType<*>>()
private val typeToCodeValueType = mutableMapOf<KClass<*>, CodeValueType<*>>()

private fun placeholder(n: String) = CodeValueType(n, PlaceholderVal::class) { TODO("placeholder!") }

// reflection
val VALUE_TYPE_NULL = CodeValueType("NULL", NullVal::class) { NullVal }
val VALUE_TYPE_TYPE = CodeValueType("TYPE", CodeValueType::class, ::getCodeValueType)
val VALUE_TYPE_PARAM = placeholder("PARAM")
val VALUE_TYPE_VAR = placeholder("VAR")
val VALUE_TYPE_CONST = placeholder("CONST")
val VALUE_TYPE_EVENT_VAL = CodeValueType("EVENT_VAL", EventValue::class, ::getEventVal)
val VALUE_TYPE_TARGET = CodeValueType("TARGET", EventTarget::class, ::getEventTarget)
val VALUE_TYPE_FUNC = CodeValueType("FUNC", InstListLabel::class, ::dataLabel)
// primitive
val VALUE_TYPE_STR = CodeValueType("STR", StrVal::class) { StrVal(it) }
val VALUE_TYPE_NUM = CodeValueType("NUM", NumVal::class) { NumVal(it.toFloat()) }
val VALUE_TYPE_BOOL = CodeValueType("BOOL", BoolVal::class) { BoolVal(it.toBoolean()) }
val VALUE_TYPE_LIST = placeholder("LIST")
val VALUE_TYPE_EXPR = placeholder("EXPR") //TODO: evaluate vars n math and concat them to strings or used as a number
// minecraft
val VALUE_TYPE_TXT = CodeValueType("TXT", TxtVal::class) { TxtVal(MM.deserialize(it)) }
val VALUE_TYPE_ITEM = placeholder("ITEM") //TODO: this is annoying because i have to waste storing the itemstack for mc and the item data for hyperstom
val VALUE_TYPE_PARTICLE = placeholder("PARTICLE") //TODO: bunch of fields

private object CodeValueTypeSerializer : IdentifiableSerializer<CodeValueType<*>>(::getCodeValueType)
@Serializable(CodeValueTypeSerializer::class) data class CodeValueType<T : CodeValBox>(
    override val name: String,
    val typeClass: KClass<T>,
    private val getter: (name: String) -> T
) : CodeValBox {
    init {
        nameToCodeValueType[name] = this
        typeToCodeValueType[typeClass] = this
    }
    fun deserialize(data: String) = getter(data)
}

@Serializable data class CodeValue<T : CodeValBox>(val type: CodeValueType<T>, val value: T)
val NULL_VALUE = CodeValue(VALUE_TYPE_NULL, NullVal)
