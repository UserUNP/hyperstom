package userunp.hyperstom.datastore

import userunp.hyperstom.code.*
import userunp.hyperstom.world.ContributorLevel
import userunp.hyperstom.world.WorldManager
import kotlinx.serialization.Serializable
import net.minestom.server.event.trait.InstanceEvent
import userunp.hyperstom.CodeException
import java.util.UUID

@Serializable @DataStoreRecord("contributors")
data class StoreWorldContributors(private val map: MutableMap<String, ContributorLevel>) {
    fun hasPerm(uuid: UUID, level: ContributorLevel) = map[uuid.toString()]?.hasPerm(level) ?: false
    operator fun set(uuid: UUID, level: ContributorLevel) {
        map[uuid.toString()] = level
    }

    companion object : PersistentStoreCompanion {
        override val default = StoreWorldContributors(mutableMapOf())
    }
}

@Serializable @DataStoreRecord("code")
data class StoreWorldCode(private val labels: InstLabelMap) {
    operator fun set(label: InstListLabel, inst: Instruction) = labels.getOrPut(label) { mutableListOf() }.add(inst)
    operator fun invoke(msEvent: InstanceEvent, entryLabel: InstListLabel, world: WorldManager) {
        val entryInstList = labels[entryLabel] ?: return
        val invokable = entryLabel.type.get(entryLabel.name)
        val ctx = InvokeContext(invokable, msEvent, entryInstList, entryLabel)
        try {
            world.runtimeInvoker.exec(ctx, this, 0)
        } catch (e: Exception) { throw CodeException(e) }
        //TODO: world specific logs that developers can view to debug their code
    }

    fun getLabels() = labels.keys
    fun getInstList(label: InstListLabel) = labels[label]

    companion object : PersistentStoreCompanion {
        override val default = StoreWorldContributors(mutableMapOf())
    }
}
