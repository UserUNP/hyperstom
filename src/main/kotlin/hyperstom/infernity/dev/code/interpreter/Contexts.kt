package hyperstom.infernity.dev.code.interpreter

import hyperstom.infernity.dev.code.block.CodeBlock.ContextResolver
import hyperstom.infernity.dev.event.HSEvent
import hyperstom.infernity.dev.world.WorldManager
import net.minestom.server.entity.Entity
import net.minestom.server.utils.entity.EntityFinder.TargetSelector

interface InstructionContext {
    val world: WorldManager
}

data class LocalThreadContext(override val world: WorldManager, val targetSelector: TargetSelector, var defaultEntity: Entity?) : InstructionContext

data class InterpreterContext<T : HSEvent>(val event: T) : InstructionContext {
    override val world = event.world
}

val actionCtxResolver = ContextResolver { sign, ctx: LocalThreadContext ->
    LocalThreadContext(ctx.world, if (sign.l4.isNotEmpty()) enumValueOf<TargetSelector>(sign.l4) else ctx.targetSelector, ctx.defaultEntity)
}
