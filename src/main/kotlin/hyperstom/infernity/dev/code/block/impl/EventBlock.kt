package hyperstom.infernity.dev.code.block.impl

import hyperstom.infernity.dev.code.block.InterpreterCodeBlock
import hyperstom.infernity.dev.code.interpreter.InterpreterContext
import hyperstom.infernity.dev.event.HSEvent

class EventBlock(props: Properties) : InterpreterCodeBlock<HSEvent>(props) {
    override fun interpret(ctx: InterpreterContext<HSEvent>) {
    }
}
