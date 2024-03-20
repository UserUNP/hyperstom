@file:OptIn(ExperimentalEncodingApi::class)

package dev.bedcrab.hyperstom.code

import dev.bedcrab.hyperstom.MM
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.jvm.internal.impl.resolve.constants.NullValue

private val LOGGER = KotlinLogging.logger {}

fun getCodeValueType(type: String) = nameToCodeValueType[type] ?: throw RuntimeException("Unsupported code value type! $type")
private val nameToCodeValueType = mutableMapOf<String, CodeValueType<*>>()
// reflection
val NULL_VALUE_TYPE = CodeValueType("NULL") { SimpleHSValue(NullValue()) }
val TYPE_VALUE_TYPE = CodeValueType<Nothing>("TYPE") { TODO() }
val PARAM_VALUE_TYPE = CodeValueType<Nothing>("PARAM") { TODO() }
val VAR_VALUE_TYPE = CodeValueType<Nothing>("VAR") { TODO() }
val CONST_VALUE_TYPE = CodeValueType<Nothing>("CONST") { TODO() }
val EVENT_VALUE_TYPE = CodeValueType<Nothing>("EVENT_VALUE") { TODO() }
val FUNC_VALUE_TYPE = CodeValueType<Nothing>("FUNC") { TODO() }
// primitive
val STR_VALUE_TYPE = CodeValueType("STRING") { SimpleHSValue(it) }
val INT_VALUE_TYPE = CodeValueType("INT") { SimpleHSValue(it.toInt()) }
val FLOAT_VALUE_TYPE = CodeValueType("FLOAT") { SimpleHSValue(it.toFloat()) }
val BOOL_VALUE_TYPE = CodeValueType("BOOL") { SimpleHSValue(it.toBoolean()) }
val LIST_VALUE_TYPE = CodeValueType<Nothing>("LIST") { TODO() }
val EXPR_VALUE_TYPE = CodeValueType<Nothing>("EXPRESSION") { TODO() }
// minecraft
val TEXT_VALUE_TYPE = CodeValueType("TEXT") { SimpleHSValue(MM.deserialize(it)) }
val ITEM_VALUE_TYPE = CodeValueType<Nothing>("ITEM") { TODO() }
val PARTICLE_VALUE_TYPE = CodeValueType<Nothing>("PARTICLE") { TODO() }

class CodeValueType<T>(val name: String, private val getter: (input: String) -> HSValue<T, *>) {
    operator fun invoke(input: String): HSValue<T, *> = getter(input)
    init {
        nameToCodeValueType[name] = this
        LOGGER.info { "Registered code value type $name@${hashCode()}" }
    }
}


private typealias SimpleHSValue<T> = HSValue<T, Nothing>
data class HSValue<T, S>(val data: T, val subtype: CodeValueType<S>? = null)

fun <T> codeValueEntry(type: CodeValueType<T>, data: T) = CodeValueEntry(type.name, data.toString())
@Serializable data class CodeValueEntry(val type: String, val value: String)
