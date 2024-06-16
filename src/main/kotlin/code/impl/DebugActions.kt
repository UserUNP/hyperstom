package userunp.hyperstom.code.impl

import net.kyori.adventure.text.Component
import userunp.hyperstom.code.*

object PrintInstructions : InstFunction {
    override val params = arrayOf<Parameter<out CodeValBox>>()
    override fun invoke(ctx: InstContext) {
        ctx.instance.sendMessage(Component.text(ctx.controller.frame.instList.toString()))
    }
}

object DebugInstruction : InstFunction {
    private val strParam = Parameter("first", VALUE_TYPE_STR)
    private val boolParam = Parameter("second", VALUE_TYPE_BOOL)
    override val params = arrayOf(strParam, boolParam)
    override fun invoke(ctx: InstContext) {
        ctx.instance.sendMessage(Component.text("\ntarget: ${ctx.target}\nargs: ${ctx.inst.args.map}"))
        val str = ctx.inst.args.single(strParam)
        ctx.instance.sendMessage(Component.text("Parsed first arg: $str"))
        val bool = ctx.inst.args.single(boolParam)
        ctx.instance.sendMessage(Component.text("Parsed second arg: $bool"))
    }
}
