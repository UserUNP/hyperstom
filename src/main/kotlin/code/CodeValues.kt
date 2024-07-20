@file:Suppress("UnstableApiUsage")

package ma.userunp.hyperstom.code

import ma.userunp.hyperstom.*
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.NetworkBuffer
import net.minestom.server.particle.Particle
import net.minestom.server.potion.PotionEffect
import net.minestom.server.sound.SoundEvent
import net.kyori.adventure.text.TextComponent

interface CodeValType<T : Any> : Named, NetworkBuffer.Type<T>

private fun <T : Any> implType(n: String, t: NetworkBuffer.Type<T>) =
    object : CodeValType<T>, NetworkBuffer.Type<T> by t { override val name = n }
private fun <T : Named> implNamedType(n: String, r: NamedRegistry<T>) = implType(n, ValTypeStr.map(r::get, Named::name))

private object PlaceholderType : NetworkBuffer.Type<Nothing> {
    override fun write(buffer: NetworkBuffer, value: Nothing?) { TODO("Not yet implemented") }
    override fun read(buffer: NetworkBuffer): Nothing { TODO("Not yet implemented") }
}

private fun placeholderType(n: String) = implType(n, PlaceholderType)

private object NullType : NetworkBuffer.Type<Unit> {
    override fun write(buffer: NetworkBuffer, value: Unit?) {}
    override fun read(buffer: NetworkBuffer) {}
}

class CodeTxt(val txt: String, comp: TextComponent) : TextComponent by comp
fun txtVal(t: String) = CodeTxt(t, MM.deserialize(t) as TextComponent)

// primitive

object ValTypeNull : CodeValType<Unit> by implType("NUL", NullType)
object ValTypeStr : CodeValType<String> by implType("STR", NetworkBuffer.STRING)
object ValTypeNum : CodeValType<Float> by implType("NUM", NetworkBuffer.FLOAT)
object ValTypeBool : CodeValType<Boolean> by implType("BLN", NetworkBuffer.BOOLEAN)
object ValTypeExpr : CodeValType<Nothing> by placeholderType("XPR") //TODO: evaluate vars n math and concat them to strings or used as a number
object ValTypeList : CodeValType<Nothing> by placeholderType("ARR") //TODO: primitive, but its actually between reflection & internal and this is annoying

// reflection

object ValTypeParamType : CodeValType<ParamType<*>> by implNamedType("PTYP", RegistryParamTypes) //TODO: should not be restricted to a registry but whatever there's no struct or need for runtime param types
object ValTypeLabelType : CodeValType<CodeLabelType<*>> by implNamedType("LTYP", RegistryLabelTypes)
object ValTypeLabel : CodeValType<CodeLabel<*>> by implType("L", object : NetworkBuffer.Type<CodeLabel<*>> {
    override fun write(buffer: NetworkBuffer, value: CodeLabel<*>) {
        ValTypeLabelType.write(buffer, value.type)
        ValTypeStr.write(buffer, value.label)
    }
    override fun read(buffer: NetworkBuffer) = CodeLabel(ValTypeLabelType.read(buffer), ValTypeStr.read(buffer))
})
object ValTypeTarget : CodeValType<EventTarget<*>> by implNamedType("TRGT", RegistryTargets)
object ValTypeEventVal : CodeValType<EventVal<*, *>> by implNamedType("EVAL", RegistryEventVals)
object ValTypeVar : CodeValType<String> by implType("V", ValTypeStr)
object ValTypeParam : CodeValType<Nothing> by placeholderType("P")
object ValTypeGlobal : CodeValType<Nothing> by placeholderType("G")

// internal (the C stands for Code (not the lang C))

object ValTypeCType : CodeValType<CodeValType<*>> by implNamedType("#", RegistryValTypes)
@Suppress("UNCHECKED_CAST")
object ValTypeCVal : CodeValType<CodeVal<*>> by implType("#", object : NetworkBuffer.Type<CodeVal<*>> {
    override fun write(buffer: NetworkBuffer, value: CodeVal<*>) {
        ValTypeCType.write(buffer, value.type)
        (value.type as CodeValType<Any>).write(buffer, value.value)
    }
    override fun read(buffer: NetworkBuffer): CodeVal<*> {
        val type = ValTypeCType.read(buffer)
        if (type is ValTypeNull) return NULL_VALUE
        val value = type.read(buffer)
        return CodeVal(type as CodeValType<Any>, value)
    }
})
object ValTypeCInstType : CodeValType<InstType> by implNamedType("#", RegistryInstTypes)
object ValTypeCInst : CodeValType<CodeInst> by implType("#", object : NetworkBuffer.Type<CodeInst> {
    override fun write(buffer: NetworkBuffer, value: CodeInst) {
        ValTypeCInstType.write(buffer, value.type)
        ValTypeTarget.write(buffer, value.target)
        buffer.writeCollection(ValTypeCVal, value.rawArgs)
    }
    override fun read(buffer: NetworkBuffer): CodeInst {
        val props = ValTypeCInstType.read(buffer)
        val target = ValTypeTarget.read(buffer)
        val args = buffer.readCollection(ValTypeCVal, Int.MAX_VALUE)
        return CodeInst(props, args, target)
    }
})

// minecraft

object ValTypeTxt : CodeValType<CodeTxt> by implType("TXT", ValTypeStr.map(::txtVal, CodeTxt::txt))
object ValTypeMat : CodeValType<Material> by implType("MAT", Material.NETWORK_TYPE)
object ValTypePot : CodeValType<PotionEffect> by implType("POT", PotionEffect.NETWORK_TYPE)
object ValTypeVfx : CodeValType<Particle> by implType("VFX", Particle.NETWORK_TYPE)
object ValTypeSnd : CodeValType<SoundEvent> by implType("SND", SoundEvent.NETWORK_TYPE)
object ValTypeItem : CodeValType<ItemStack> by implType("ITEM", ItemStack.STRICT_NETWORK_TYPE)

fun <T : Any> CodeValType<T>.get(value: T) = CodeVal(this, value)

fun CodeValType<*>.isRuntime() = when (this) {
    ValTypeVar, ValTypeEventVal -> true
    else -> false
}

class CodeVal<T : Any>(val type: CodeValType<T>, val value: T)
val NULL_VALUE = CodeVal(ValTypeNull, Unit)
fun CodeVal<*>.isNull() = this == NULL_VALUE
