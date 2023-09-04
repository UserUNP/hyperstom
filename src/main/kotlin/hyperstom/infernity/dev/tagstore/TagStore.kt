@file:Suppress("UnstableApiUsage")

package hyperstom.infernity.dev.tagstore

import hyperstom.infernity.dev.TAG_STORE_HOME
import net.minestom.server.entity.Player
import net.minestom.server.tag.Tag
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

class TagStore(private val player: Player) {
    private fun nbt() = player.getTag(TAG_STORE_HOME) as NBTCompound? ?: NBTCompound.EMPTY

    fun <T> read(type: KClass<T>): T where T : Record, T : StoreData {
        val name = type.java.simpleName.lowercase()
        assert(type.isData && type.java.isRecord) { "$name is not a data/record class!" }
        assert(type.companionObjectInstance is StoreComp) { "$name's companion object is not TagStoreComp!" }
        val companion = type.companionObjectInstance as StoreComp

        val nbt = nbt()
        var compound = nbt.getCompound(name) ?: NBTCompound.EMPTY
        val updatedCompound = compound.toMutableCompound()
        for (c in type.java.recordComponents) if (compound[c.name] == null) {
            updatedCompound[c.name] = companion.defaultFunc(c.name)
        }
        compound = updatedCompound.toCompound()
        return Tag.View(type.java).read(compound) ?: throw NullPointerException("Couldn't read session data `$name`!")
    }

    fun <T> write(value: T) where T : Record, T : StoreData {
        val name = value.javaClass.simpleName.lowercase()
        assert(value.javaClass.isRecord) { "$name is not a data/record class" }
        val newNBT = NBTCompound.EMPTY.toMutableCompound()
        val rootNBT = nbt().toMutableCompound()
        Tag.View(value.javaClass).write(newNBT, value)
        rootNBT[name] = newNBT.toCompound()
        player.setTag(TAG_STORE_HOME, rootNBT.toCompound())
    }

    companion object {
        fun <T> tag(type: KClass<T>): Tag<T> where T : Record, T : StoreData {
            val name = type.java.simpleName.lowercase()
            assert(type.isData && type.java.isRecord) { "$name is not a data/record class!" }
            assert(type.companionObjectInstance is StoreComp) { "$name's companion object is not TagStoreComp!" }
            return Tag.Structure(name, type.java).path(TAG_STORE_HOME.key)
        }

        interface StoreData
        interface StoreComp {
            fun defaultFunc(missing: String): NBT
        }
    }
}
