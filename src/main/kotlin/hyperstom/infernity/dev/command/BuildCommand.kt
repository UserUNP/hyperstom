package hyperstom.infernity.dev.command

import hyperstom.infernity.dev.plot.mode.ModeHandler
import hyperstom.infernity.dev.tagstore.StorePlotState

class BuildCommand : HSCommand("build") {
    init {
        Syntax { _, store, _ ->
            val state = store.read(StorePlotState::class)
            store.write(state.withMode(ModeHandler.Mode.BUILD))
        }
    }
}
