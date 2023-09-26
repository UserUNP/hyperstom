package hyperstom.infernity.dev.event

import hyperstom.infernity.dev.world.WorldManager
import net.minestom.server.entity.Player

class PlayerJoinEvent(world: WorldManager, player: Player) : HSEvent(world) {
    init {
        defaultEntity = player
    }
}
