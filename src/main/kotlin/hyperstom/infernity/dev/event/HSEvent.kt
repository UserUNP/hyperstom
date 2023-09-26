package hyperstom.infernity.dev.event

import hyperstom.infernity.dev.world.WorldManager
import net.minestom.server.entity.Entity
import net.minestom.server.event.trait.InstanceEvent

abstract class HSEvent(val world: WorldManager) : InstanceEvent {
    var defaultEntity: Entity? = null
    override fun getInstance() = world.play
}
