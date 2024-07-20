package ma.userunp.hyperstom.code.impl

import ma.userunp.hyperstom.MM
import ma.userunp.hyperstom.code.InstContext
import ma.userunp.hyperstom.code.InstType
import ma.userunp.hyperstom.code.ParamNode
import ma.userunp.hyperstom.code.TargetClass

object InstDebugFrame : InstType {
    override val name = "DEBUG1"
    override val targetClass = TargetClass.NONE
    override val params = arrayOf<ParamNode<*>>()
    override fun exec(ctx: InstContext) = ctx.frame.let {
        ctx.instance.sendMessage(MM.deserialize(buildString {
            it.label.run { appendLine("Label: $label ($type)") }
            it.previous?.label?.run { appendLine("Previous label: $label ($type)") }
            appendLine("Instructions: ${it.instList.size} ${it.instList.joinToString { it.type.name }}")
            appendLine("Variables: ${it.vars.mapValues { it.value.type }}")
        }))
    }
}
