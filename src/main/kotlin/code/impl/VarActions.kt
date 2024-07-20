package ma.userunp.hyperstom.code.impl

import ma.userunp.hyperstom.Named
import ma.userunp.hyperstom.code.*

// any

object InstAssignVar : InstType {
    override val name = "SET"
    override val targetClass = TargetClass.NONE
    private val varParam = ParamSingle("var", ParamTypeVar)
    private val valParam = ParamSingle("val", ParamTypeAny)
    private val typeParam = ParamSingle("type", ParamOptType(ParamTypeType, ValTypeParamType.get(ParamTypeAny)))
    override val params = arrayOf(varParam, valParam, typeParam)
    override fun exec(ctx: InstContext) {
        ctx.argCodeVal(valParam)[0].let { c -> ctx.arg(typeParam)[0].let { t -> ctx.arg(varParam)[0].let {
            if (!t.check(c.type)) throw RuntimeException("Var $it expects type ${t.name}, but got ${c.type.name} instead!")
            ctx.frame.vars[name] = c
        } } }
    }
}

// txt

object InstToTxt : InstType {
    override val name = "TXT"
    override val targetClass = TargetClass.NONE
    private val varParam = ParamSingle("var", ParamTypeVar)
    private val valsParam = ParamMulti("vals", ParamTypeAny)
    override val params = arrayOf(varParam, valsParam)
    override fun exec(ctx: InstContext) {
        ctx.frame.vars[ctx.arg(varParam)[0]] =
            ValTypeTxt.get(txtVal(buildString { for (v in ctx.arg(valsParam)) when (v) {
                is Named -> v.name
                else -> v.toString()
            } }))
    }
}
