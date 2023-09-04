package hyperstom.infernity.dev.code.action

import net.kyori.adventure.text.TextComponent
import net.minestom.server.entity.Entity
import net.minestom.server.instance.Instance

interface CodeAction {
    val description: List<TextComponent>
    fun execute(instance: Instance, entity: Entity)
    //TODO: make Arguments interface that defines the args for it's action and getter & setter methods that could set/get to null (if missing)

    abstract class Registry {
        abstract val defaultAction: CodeAction
        abstract fun get(index: Int): CodeAction
    }
}
