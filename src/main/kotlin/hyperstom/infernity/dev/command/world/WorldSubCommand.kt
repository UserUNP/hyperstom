package hyperstom.infernity.dev.command.world

import hyperstom.infernity.dev.command.HSCommand
import net.minestom.server.command.builder.arguments.Argument

abstract class WorldSubCommand(name: String, private val perm: Permission?) : HSCommand(name) {

    inner class Syntax(vararg arguments: Argument<*>, executor: CommandExecutor) : HSCommand.Syntax(arguments, executor, { player, store, context ->
        true // TODO: permissions saved into world properties file (inside archive (gets parsed using WorldArchiveIO))
    })

    enum class Permission { ANY, DEV, BUILDER, ADMIN, OWNER }
}
