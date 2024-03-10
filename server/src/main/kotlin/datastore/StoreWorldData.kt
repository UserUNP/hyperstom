package dev.bedcrab.hyperstom.datastore

import dev.bedcrab.hyperstom.code.*
import dev.bedcrab.hyperstom.world.ContributorLevel
import dev.bedcrab.hyperstom.world.WorldManager
import kotlinx.serialization.Serializable
import net.minestom.server.entity.Entity
import net.minestom.server.event.trait.InstanceEvent
import java.util.UUID

@JvmRecord
@Serializable
@DataStoreRecord("contributors")
data class StoreWorldContributors(private val map: MutableMap<String, ContributorLevel>) {
    fun hasPerm(uuid: UUID, level: ContributorLevel) = map[uuid.toString()]?.hasPerm(level) ?: false
    operator fun set(uuid: UUID, level: ContributorLevel) {
        map[uuid.toString()] = level
    }

    companion object : PersistentStoreCompanion {
        override val default = StoreWorldContributors(mutableMapOf())
    }
}

@JvmRecord
@Serializable
@DataStoreRecord("code")
data class StoreWorldCode(private val map: MutableMap<CodeBlock.TypeEntry, InstList>) {
    operator fun set(entry: CodeBlock.TypeEntry, inst: Instruction) {
        val list = map[entry] ?: mutableListOf<Instruction>().also { map[entry] = it }
        list.add(inst)
    }
    operator fun invoke(
        msEvent: InstanceEvent?, entry: CodeBlock.TypeEntry, world: WorldManager, selection: MutableList<Entity>
    ) {
        val type = CodeBlock.Type.from(entry.type)
        if (type == DATA_TYPE) throw  RuntimeException("Data types aren't supported yet!")
        val instructions = map[entry] ?: return
        val invokable = type(entry.data)
        invokable(InvokeContext(msEvent, world, instructions, selection))
        // TODO: keep track of exec controllers
    }
    // TODO: world specific logs that developers can view to debug their code

    companion object : PersistentStoreCompanion {
        override val default = StoreWorldCode(mutableMapOf())
    }
}
