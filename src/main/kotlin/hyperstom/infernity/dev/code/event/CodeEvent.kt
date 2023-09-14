package hyperstom.infernity.dev.code.event

import hyperstom.infernity.dev.code.interpreter.InterpreterThread
import net.kyori.adventure.text.Component

interface CodeEvent {
    val description: List<Component>
    fun interpreterThread(): InterpreterThread

    abstract class Registry {
        abstract fun get(index: Int): CodeEvent
    }
}
