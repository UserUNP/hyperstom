package hyperstom.infernity.dev.command

import hyperstom.infernity.dev.command.world.WorldCreateCommand
import hyperstom.infernity.dev.command.world.WorldSaveCommand

class WorldCommand : HSCommand("world") {
    init {
        addSubcommand(WorldCreateCommand())
        addSubcommand(WorldSaveCommand())
    }
}
