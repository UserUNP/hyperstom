package hyperstom.infernity.dev.command

import hyperstom.infernity.dev.command.world.WorldCreateCommand

class WorldCommand : HSCommand("world") {
    init {
        addSubcommand(WorldCreateCommand())
    }
}
