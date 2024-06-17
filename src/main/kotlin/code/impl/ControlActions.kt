package userunp.hyperstom.code.impl

import userunp.hyperstom.code.InstContext
import userunp.hyperstom.code.InstFunction
import userunp.hyperstom.code.Parameter
import userunp.hyperstom.code.VALUE_TYPE_FUNC

object CallFunction : InstFunction {
    private val funcParam = Parameter("func", VALUE_TYPE_FUNC)
    override val params = arrayOf(funcParam)
    override fun invoke(ctx: InstContext) {
        val label = ctx.inst.args.single(funcParam)
        ctx.controller.jumpTo(label, 0)
    }
}
