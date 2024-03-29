package dev.bedcrab.hyperstom.datastore

import dev.bedcrab.hyperstom.code.*
import dev.bedcrab.hyperstom.world.ContributorLevel
import dev.bedcrab.hyperstom.world.WorldManager
import kotlinx.serialization.Serializable
import net.minestom.server.event.trait.InstanceEvent
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
data class StoreWorldCode(private val map: MutableMap<RootCodeBlockEntry, InstList>) {
    operator fun set(entry: RootCodeBlockEntry, inst: Instruction) {
        val list = map[entry] ?: mutableListOf<Instruction>().also { map[entry] = it }
        list.add(inst)
    }
    operator fun invoke(
        msEvent: InstanceEvent, entry: RootCodeBlockEntry, world: WorldManager
    ) {
        val type = getCodeBlockType(entry.type)
        val instructions = map[entry] ?: return
        val invokable = type(entry.data)
        /*val controller = */invokable(InvokeContext(world, msEvent, instructions))
        // TODO: keep track of exec controllers
    }
    // TODO: world specific logs that developers can view to debug their code

    companion object : PersistentStoreCompanion {
        override val default = StoreWorldContributors(mutableMapOf())
    }
}
