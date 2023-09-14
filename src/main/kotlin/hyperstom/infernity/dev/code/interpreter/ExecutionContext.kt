package hyperstom.infernity.dev.code.interpreter

import net.minestom.server.entity.Entity
import net.minestom.server.instance.Instance

data class ExecutionContext(val instance: Instance) {
    var entity: Entity? = null
}
