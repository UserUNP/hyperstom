package userunp.hyperstom.code.impl

import userunp.hyperstom.code.InstContext
import userunp.hyperstom.code.InstFunction
import userunp.hyperstom.code.Parameter
import userunp.hyperstom.code.VAL_TYPE_VAR

object AssignVar : InstFunction {
    private val varParam = Parameter("var", VAL_TYPE_VAR)
    private val valParam = Parameter<Nothing>("val", dynamicType = true)
    override val params = arrayOf(varParam)
    override fun invoke(ctx: InstContext) {
        val name = ctx.controller.arg(varParam).value.name
        ctx.controller.frame.vars[name] = ctx.controller.argAny(valParam)
    }
}
