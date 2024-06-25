package userunp.hyperstom.datastore

import userunp.hyperstom.world.ContributorLevel
import kotlinx.serialization.Serializable
import net.minestom.server.event.trait.InstanceEvent
import userunp.hyperstom.CodeException
import userunp.hyperstom.code.*
import java.util.UUID

@Serializable @DataStoreRecord("Contributors")
data class StoreWorldContributors(private val map: MutableMap<String, ContributorLevel>) {
    fun hasPerm(uuid: UUID, level: ContributorLevel) = map[uuid.toString()]?.hasPerm(level) ?: false
    operator fun set(uuid: UUID, level: ContributorLevel) {
        map[uuid.toString()] = level
    }

    companion object : PersistentStoreCompanion {
        override val default = StoreWorldContributors(mutableMapOf())
    }
}

@Serializable @DataStoreRecord("Code")
data class StoreWorldCode(private val labels: InstLabelMap) : CodeLabelResolver {
    operator fun set(label: CodeLabel, inst: Instruction) = labels.getOrPut(label) { mutableListOf() }.add(inst)
    operator fun invoke(msEvent: InstanceEvent, entryLabel: CodeLabel, invoker: Invoker) {
        val entryInstList = labels[entryLabel] ?: return
        val invokable = entryLabel.type.get(entryLabel.name)
        val ctx = InvokeContext(invokable, msEvent, entryInstList, entryLabel)
        try {
            invoker.exec(ctx, 0, this)
        } catch (e: Exception) { throw CodeException(e) }
        //TODO: world specific logs that developers can view to debug their code
    }

    fun getLabels() = labels.keys
    override fun resolveLabel(label: CodeLabel) = labels[label] ?: throw RuntimeException("No such label! ${label.name}")

    companion object : PersistentStoreCompanion {
        override val default = StoreWorldCode(mutableMapOf())
    }
}
