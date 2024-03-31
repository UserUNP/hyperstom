@file:OptIn(ExperimentalEncodingApi::class)

package dev.bedcrab.hyperstom.code

import dev.bedcrab.hyperstom.MM
import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.KClass
import kotlin.reflect.jvm.internal.impl.resolve.constants.NullValue

fun getCodeValueType(type: String) = nameToCodeValueType[type] ?: throw RuntimeException("Unsupported code value type! $type")
private val nameToCodeValueType = mutableMapOf<String, CodeValueType<*>>()
private val typeToCodeValueType = mutableMapOf<KClass<*>, CodeValueType<*>>()
// reflection
val NULL_VALUE_TYPE = regType("NULL", { NullValue() }, { "" })
val TYPE_VALUE_TYPE = regType("TYPE", ::getCodeValueType, CodeValueType<*>::name)
val PARAM_VALUE_TYPE = regType("PARAM", ::TODO, ::TODO)
val VAR_VALUE_TYPE = regType("VAR", ::TODO, ::TODO)
val CONST_VALUE_TYPE = regType("CONST", ::TODO, ::TODO)
val EVENT_VALUE_TYPE = regType("EVENT_VALUE", { AdvancedHSValue(it) { d -> getEventVal(d) } }, { it.hsVal.name })
val TARGET_VALUE_TYPE = regType("TARGET_VALUE", { AdvancedHSValue(it) { d -> getCodeTarget(d) } }, { it.hsVal.name })
val FUNC_VALUE_TYPE = regType("FUNC", ::TODO, ::TODO)
// primitive
val STR_VALUE_TYPE = regType("STR", { it }, { it })
val INT_VALUE_TYPE = regType("INT", { it.toInt() }, { it.toString() })
val FLOAT_VALUE_TYPE = regType("FLOAT", { it.toFloat() }, { it.toString() })
val BOOL_VALUE_TYPE = regType("BOOL", { it.toBoolean() }, { it.toString() } )
val LIST_VALUE_TYPE = regType("LIST", ::TODO, ::TODO)
val EXPR_VALUE_TYPE = regType("EXPR", ::TODO, ::TODO)
// minecraft
val TEXT_VALUE_TYPE = regType("TEXT", { MM.deserialize(it) }, { MM.serialize(it) } )
val ITEM_VALUE_TYPE = regType("ITEM", ::TODO, ::TODO)
val PARTICLE_VALUE_TYPE = regType("PARTICLE", ::TODO, ::TODO)

fun Any?.asCodeValueType() = this?.let { typeToCodeValueType[it::class] } ?: NULL_VALUE_TYPE
private inline fun <reified T> regType(name: String, noinline reader: (input: String) -> T, noinline writer: (input: T) -> String)
    = CodeValueType(name, reader, writer).also { typeToCodeValueType[T::class] = it }

data class CodeValueType<T>(val name: String, private val reader: (input: String) -> T, private val writer: (input: T) -> String) {
    operator fun invoke(input: String) = reader(input)
    fun serialize(input: T) = writer(input)
    init { nameToCodeValueType[name] = this }
}

class AdvancedHSValue<T>(data: String, reader: (d: String) -> T?) {
    private val parts = data.split(";")
    val type = getCodeValueType(parts[0].ifEmpty { throw RuntimeException("Unspecified type for a value of an advanced type!") })
    val hsVal = reader(Base64.decode(parts[1].ifEmpty { throw RuntimeException("What!") }).toString(Charsets.UTF_8)) ?: throw RuntimeException("Invalid advanced value!")
}

fun <T> codeValueEntry(type: CodeValueType<T>, data: T) = CodeValueEntry(type.name, type.serialize(data))
@Serializable data class CodeValueEntry(val type: String, val value: String)
