@file:OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)

package dev.bedcrab.hyperstom.code

import com.extollit.collect.SetMultiMap
import dev.bedcrab.hyperstom.world.WorldManager
import kotlinx.coroutines.*
import net.minestom.server.entity.Entity
import net.minestom.server.event.trait.InstanceEvent
import net.minestom.server.instance.Instance
import kotlin.coroutines.EmptyCoroutineContext

class ExecutionController {
    val scope = CoroutineScope(EmptyCoroutineContext)
    val threads = SetMultiMap<HSEvent, Job>()
}

data class ExecContext(val instance: Instance, val inst: Instruction)
fun interface InstFunction {
    operator fun invoke(ctx: ExecContext)
}

typealias Invokable = (ctx: InvokeContext) -> ExecutionController
data class InvokeContext(
    val msEvent: InstanceEvent?,
    val world: WorldManager,
    val instructions: InstList,
    val selection: MutableList<Entity>
)

enum class HSEvent : Invokable {
    WORLD_INITIALIZATION {
        override suspend fun getExecutee(ctx: InvokeContext): Instance {
            // TODO: disallow unsupported actions
            return ctx.world.play
        }
    },
    ;

    override fun toString() = name
    protected abstract suspend fun getExecutee(ctx: InvokeContext): Instance
    override operator fun invoke(ctx: InvokeContext) = ExecutionController().apply {
        threads.add(this@HSEvent, scope.launch(newSingleThreadContext("${ctx.world.id}:${this::class.simpleName}")) {
            try {
                val instance = getExecutee(ctx)
                // TODO: selections & variables
                for (inst in ctx.instructions) inst(instance)
            } catch (e: Exception) {
                throw RuntimeException("Runtime exception.", e)
            }
        })
    }
}
