package hyperstom.infernity.dev.command

import net.minestom.server.command.builder.Command

class AboutCommand : Command("about") {
    init {
        setDefaultExecutor { sender, _ -> sender.sendMessage("https://github.com/Hyperstom/Hyperstom") }
    }
}
