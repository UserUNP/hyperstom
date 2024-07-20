package ma.userunp.hyperstom.code

import ma.userunp.hyperstom.Named
import ma.userunp.hyperstom.ParamException
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.particle.Particle
import net.minestom.server.potion.PotionEffect
import net.minestom.server.sound.SoundEvent

class ParamNodeResult(var mark: Int, var end: Int, val raw: RawArgs, val list: Args)

interface ParamNode<T : Any> : Named {
    fun compute(result: ParamNodeResult)
}

/**
 * Param node to type check the current mark
 */
interface ParamType<T : Any> : ParamNode<T> {
    fun check(t: CodeValType<*>): Boolean
    val defaultVal: CodeVal<T>?
    override fun compute(result: ParamNodeResult) = result.raw.subList(result.mark, result.end+1).replaceAll {
        if (it.type.isRuntime()) it else if (it.isNull() && defaultVal != null) defaultVal!! else if (check(it.type)) it
        else throw ParamException("Expected $name, got ${it.type.name} instead!", result.raw.indexOf(it))
    }
}

private fun <T : Any> CodeValType<T>.paramType(): ParamType<T> = object : ParamType<T> {
    override val name = this@paramType.name
    override val defaultVal = null
    override fun check(t: CodeValType<*>) = this@paramType == t
}

class ParamSingle<T : Any>(override val name: String, private val sub: ParamNode<T>) : ParamNode<T> {
    override fun compute(result: ParamNodeResult) {
        result.end = result.mark
        sub.compute(result)
        result.list[name] = mutableListOf(result.raw[result.mark++])
    }
}

class ParamMulti<T : Any>(override val name: String, private val sub: ParamNode<T>) : ParamNode<T> {
    override fun compute(result: ParamNodeResult) = result.raw.let {
        result.end = it.lastIndex
        try { sub.compute(result) } catch (e: ParamException) {
            result.end = e.mark - 1
            sub.compute(result)
        }
        result.list[name] = it.subList(result.mark++, result.end)
    }
}

class ParamOptType<T : Any>(private val type: ParamType<T>, override val defaultVal: CodeVal<T>) : ParamType<T> {
    override val name = "${type.name}?"
    override fun check(t: CodeValType<*>) = type.check(t)
}

class ParamUnionType<T : Any, S : Any>(
    private val t1: ParamType<T>,
    private val t2: ParamType<S>,
) : ParamType<Any> {
    override val name = "${t1.name} or ${t2.name}"
    override val defaultVal = null
    override fun check(t: CodeValType<*>) = t1.check(t) || t2.check(t)
}

object ParamTypeAny : ParamType<Any> {
    override val name = "ANY"
    override val defaultVal = null
    override fun check(t: CodeValType<*>) = true
}

object ParamTypeStr : ParamType<String> by ValTypeStr.paramType()
object ParamTypeNum : ParamType<Float> by ValTypeNum.paramType()
object ParamTypeBool : ParamType<Boolean> by ValTypeBool.paramType()

object ParamTypeType : ParamType<ParamType<*>> by ValTypeParamType.paramType()
object ParamTypeLabel : ParamType<CodeLabel<*>> by ValTypeLabel.paramType()
object ParamTypeTarget : ParamType<EventTarget<*>> by ValTypeTarget.paramType()
object ParamTypeEventVal : ParamType<EventVal<*, *>> by ValTypeEventVal.paramType()
object ParamTypeVar : ParamType<String> by ValTypeVar.paramType()

object ParamTypeTxt : ParamType<CodeTxt> by ValTypeTxt.paramType()
object ParamTypeMat : ParamType<Material> by ValTypeMat.paramType()
object ParamTypePot : ParamType<PotionEffect> by ValTypePot.paramType()
object ParamTypeVfx : ParamType<Particle> by ValTypeVfx.paramType()
object ParamTypeSnd : ParamType<SoundEvent> by ValTypeSnd.paramType()
object ParamTypeItem : ParamType<ItemStack> by ValTypeItem.paramType()
