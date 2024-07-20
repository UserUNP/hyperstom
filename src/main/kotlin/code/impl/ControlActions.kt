package ma.userunp.hyperstom.code.impl

import ma.userunp.hyperstom.code.*

object InstCallFunction : InstType {
    override val name = "CALL"
    override val targetClass = TargetClass.NONE
    private val funcParam = ParamSingle("func", ParamTypeLabel)
    override val params = arrayOf(funcParam)
    override fun exec(ctx: InstContext) = ctx.jumpTo(ctx.arg(funcParam)[0], 0)
}
