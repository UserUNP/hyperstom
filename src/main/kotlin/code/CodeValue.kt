@file:OptIn(ExperimentalEncodingApi::class)

package dev.bedcrab.hyperstom.code

import dev.bedcrab.hyperstom.MM
import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.jvm.internal.impl.resolve.constants.NullValue

fun getCodeValueType(type: String) = nameToCodeValueType[type] ?: throw RuntimeException("Unsupported code value type! $type")
private val nameToCodeValueType = mutableMapOf<String, CodeValueType<*>>()
private val typedCodeValueNames = mutableListOf<String>()
// reflection
val NULL_VALUE_TYPE = CodeValueType("NULL", "<gradient:#0bb0a0:#0bb0fa>Null") { SimpleHSValue(NullValue()) }
val TYPE_VALUE_TYPE = CodeValueType("TYPE", "<gradient:#9f9cff:#5fccfc>Type") {
    val (name, subtypeName) = it.split("[<>]".toPattern())
    if (typedCodeValueNames.contains(name)) {
        if (name.isEmpty()) throw RuntimeException("Invalid type!")
        HSValue(name, getCodeValueType(subtypeName))
    } else SimpleHSValue(getCodeValueType(it))
}
val PARAM_VALUE_TYPE = CodeValueType<Nothing>("PARAM", "<gradient:#0090a0:#aaff7a:#a0f7a0>Parameter<#5fccfc>\\<>") { TODO() }
val VAR_VALUE_TYPE = CodeValueType<Nothing>("VAR", "<gradient:#709f0f:#e3f000:#fe9e00>Variable") { TODO() }
val CONST_VALUE_TYPE = CodeValueType<Nothing>("CONST", "<gradient:#7f5f5f:#e4660c:red>Constant<#5fccfc>\\<>") { TODO() }
val EVENT_VALUE_TYPE = CodeValueType<Nothing>("EVENT_VALUE", "<gradient:#ffd070:#ffffaf:#ffd070>Event Value") { TODO() }
val FUNC_VALUE_TYPE = CodeValueType<Nothing>("FUNC", "<gradient:#0adadf:#0f7fff:#0acacf:#0f7fff>Function ref") { TODO() }
// primitive
val STRING_VALUE_TYPE = CodeValueType("STRING", "<red>Str") { SimpleHSValue(it) }
val INT_VALUE_TYPE = CodeValueType("INT", "<red>Int") { SimpleHSValue(it.toInt()) }
val FLOAT_VALUE_TYPE = CodeValueType("FLOAT", "<red>Float") { SimpleHSValue(it.toFloat()) }
val BOOL_VALUE_TYPE = CodeValueType("BOOL", "<red>Bool") { SimpleHSValue(it.toBoolean()) }
val LIST_VALUE_TYPE = CodeValueType("LIST", "<red>List<#5fccfc>\\<>") {
    val (name, b64Contents) = it.split(";")
    val type = getCodeValueType(name)
    val contents = Base64.decode(b64Contents).toString(Charsets.UTF_8).split("§").map(type::invoke)
    HSValue(contents, type)
}
val EXPRESSION_VALUE_TYPE = CodeValueType<Nothing>("EXPRESSION", "<red>Expr<#5fccfc>\\<>") { TODO() }
// minecraft
val TEXT_VALUE_TYPE = CodeValueType("TEXT", "<green>Txt") { SimpleHSValue(MM.deserialize(it)) }
val ITEM_VALUE_TYPE = CodeValueType<Nothing>("ITEM", "<gold>Item") { TODO() }
val PARTICLE_VALUE_TYPE = CodeValueType<Nothing>("PARTICLE", "<purple>Particle") { TODO() }

class CodeValueType<T>(val name: String, mm: String, private  val getter: (input: String) -> HSValue<T, *>) {
    val label = MM.stripTags(mm)
    val componentLabel = MM.deserialize(mm)
    operator fun invoke(input: String): HSValue<T, *> = getter(input)
    init {
        nameToCodeValueType[name] = this
        if (name.endsWith("<>")) typedCodeValueNames.add(name)
        // TODO: redo typed code values because this is stupid
    }
}

private typealias SimpleHSValue<T> = HSValue<T, Nothing>
data class HSValue<T, S>(val data: T, val subtype: CodeValueType<S>? = null)

fun <T> codeValueEntry(type: CodeValueType<T>, data: T) = CodeValueEntry(type.name, data.toString())
@Serializable data class CodeValueEntry(val type: String, val value: String)
