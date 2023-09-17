package hyperstom.infernity.dev.code.event

import hyperstom.infernity.dev.code.interpreter.InterpreterThread
import net.kyori.adventure.text.Component

interface CodeEvent {
    val isDefault: Boolean
    val description: List<Component>
    fun interpreterThread(): InterpreterThread

    abstract class Registry {
        abstract val default: CodeEvent
        abstract fun get(index: Int): CodeEvent
        abstract fun get(name: String): CodeEvent
    }
}
