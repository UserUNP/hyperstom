package userunp.hyperstom.code.impl

import userunp.hyperstom.code.InstContext
import userunp.hyperstom.code.InstFunction
import userunp.hyperstom.code.Parameter
import userunp.hyperstom.code.VAL_TYPE_TXT

object SendMessage : InstFunction {
    private val msgParam = Parameter("msg", VAL_TYPE_TXT)
    override val params = arrayOf(msgParam)
    override fun invoke(ctx: InstContext) = ctx.target.sendMessage(ctx.controller.arg(msgParam).value.txt)
}
