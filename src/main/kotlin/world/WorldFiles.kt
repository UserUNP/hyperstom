package ma.userunp.hyperstom.world

import ma.userunp.hyperstom.CodeException
import ma.userunp.hyperstom.code.*
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.trait.InstanceEvent
import java.util.*

class WorldInfo(val title: String, val owner: UUID?, val spawnLoc: Pos)

enum class ContribLevel {
    ADMIN, DEVELOPER, BUILDER;
    fun hasPerm(level: ContribLevel) = level.ordinal >= ordinal
}

class WorldContrib(val map: MutableMap<String, ContribLevel>) {
    fun hasPerm(uuid: UUID, level: ContribLevel) = map[uuid.toString()]?.hasPerm(level) ?: false
    operator fun set(uuid: UUID, level: ContribLevel) {
        map[uuid.toString()] = level
    }
}

class WorldCode(private val labels: InstLabelMap) : CodeLabelResolver {
    fun set(label: CodeLabel<*>, list: InstList) = labels.put(label, list)
    fun add(label: CodeLabel<*>, inst: CodeInst) = labels.getOrPut(label) { mutableListOf() }.add(inst)
    fun invoke(msEvent: InstanceEvent, entryLabel: CodeLabel<*>, invoker: CodeInvoker) {
        val entryInstList = labels[entryLabel] ?: return
        try {
            invoker.exec(
                InvokeContext(
                entryLabel.type.get(entryLabel.label),
                msEvent, entryInstList, entryLabel
            ), this)
        } catch (e: Exception) { throw CodeException(e) }
        //TODO: world specific logs that developers can view to debug their code
    }

    fun getLabels() = labels.keys
    override fun resolveLabel(label: CodeLabel<*>) = labels[label] ?: throw RuntimeException("No such label! ${label.label}")
}
