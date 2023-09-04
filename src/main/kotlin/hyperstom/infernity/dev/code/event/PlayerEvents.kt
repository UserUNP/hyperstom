package hyperstom.infernity.dev.code.event

import hyperstom.infernity.dev.code.interpreter.InterpreterThread
import net.kyori.adventure.text.Component

private enum class PlayerCodeEvents : CodeEvent {
    Join {
        override val description: List<Component> = listOf()
        override val interpreterThread: InterpreterThread = InterpreterThread()
    },
}

object PlayerEvents : CodeEvent.Registry() {
    override fun get(index: Int): CodeEvent = PlayerCodeEvents.entries[index]
}
