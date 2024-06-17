package userunp.hyperstom.code.impl

import userunp.hyperstom.code.InstContext
import userunp.hyperstom.code.InstFunction
import userunp.hyperstom.code.Parameter
import userunp.hyperstom.code.VALUE_TYPE_TXT

object SendMessage : InstFunction {
    private val msgParam = Parameter("msg", VALUE_TYPE_TXT, plural = true)
    override val params = arrayOf(msgParam)
    override fun invoke(ctx: InstContext) {
        ctx.target.sendMessage(ctx.inst.args.single(msgParam).text)
    }
}
