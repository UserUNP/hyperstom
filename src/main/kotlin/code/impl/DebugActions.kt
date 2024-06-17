package userunp.hyperstom.code.impl

import net.kyori.adventure.text.Component
import userunp.hyperstom.code.*

object PrintInstList : InstFunction {
    override val params = arrayOf<Parameter<out CodeValBox>>()
    override fun invoke(ctx: InstContext) {
        ctx.instance.sendMessage(Component.text(ctx.controller.frame.instList.toString()))
    }
}
