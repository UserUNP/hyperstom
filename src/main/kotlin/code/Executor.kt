@file:OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)

package dev.bedcrab.hyperstom.code

import kotlinx.coroutines.*
import net.minestom.server.entity.Entity
import net.minestom.server.event.trait.InstanceEvent
import net.minestom.server.instance.Instance
import java.util.UUID
import kotlin.coroutines.EmptyCoroutineContext

class ExecutionController {
    val scope = CoroutineScope(EmptyCoroutineContext)
    val threads = mutableMapOf<HSEvent, MutableSet<Job>>()
}

data class ExecContext(val inst: Instruction, val instance: Instance, val list: InstList)
fun interface InstFunction {
    operator fun invoke(ctx: ExecContext)
}

typealias Invokable = (ctx: InvokeContext) -> ExecutionController
data class InvokeContext(
    val worldId: UUID,
    val hsEvent: InstanceEvent,
    val instructions: InstList,
    val selection: MutableList<Entity>
)

enum class HSEvent : Invokable {
    WORLD_INITIALIZATION {
        override fun validate(ctx: InvokeContext) {
            // TODO: disallow unsupported actions
        }
    },
    ;

    override fun toString() = name
    protected open fun validate(ctx: InvokeContext) {}
    override operator fun invoke(ctx: InvokeContext) = ExecutionController().apply {
        (threads[this@HSEvent] ?: mutableSetOf<Job>().also { threads[this@HSEvent] = it })
            .add(scope.launch(newSingleThreadContext("${ctx.worldId}:${this::class.simpleName}")) {
            try {
                validate(ctx)
                val instance = ctx.hsEvent.instance
                for (inst in ctx.instructions) inst(instance, ctx.instructions)
                // TODO: selections & variables
            } catch (e: Exception) {
                throw RuntimeException("Runtime exception.", e)
            }
        })
    }
}
