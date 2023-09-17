package hyperstom.infernity.dev.code.action

import hyperstom.infernity.dev.code.interpreter.ExecutionContext
import net.kyori.adventure.text.TextComponent

interface CodeAction {
    val isDefault: Boolean
    val description: List<TextComponent>
    fun execute(context: ExecutionContext)
    //TODO: make Arguments interface that defines the args for it's action and getter & setter methods that could set/get to null (if missing)

    abstract class Registry {
        abstract val default: CodeAction
        abstract fun get(index: Int): CodeAction
    }

}
