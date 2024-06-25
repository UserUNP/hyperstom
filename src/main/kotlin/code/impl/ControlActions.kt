package userunp.hyperstom.code.impl

import userunp.hyperstom.code.InstContext
import userunp.hyperstom.code.InstFunction
import userunp.hyperstom.code.Parameter
import userunp.hyperstom.code.VAL_TYPE_FUNC

object CallFunction : InstFunction {
    private val funcParam = Parameter("func", VAL_TYPE_FUNC)
    override val params = arrayOf(funcParam)
    override fun invoke(ctx: InstContext) = ctx.controller.jumpTo(ctx.controller.arg(funcParam).value, 0)
}
