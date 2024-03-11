@file:Suppress("UnstableApiUsage", "UNCHECKED_CAST")

package dev.bedcrab.hyperstom

import net.minestom.server.tag.Tag
import net.minestom.server.tag.Taggable
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmName

private val TAG_STORE_ROOT = Tag.NBT("HYPERSTOM")

abstract class DataStore<T>(private val dataProvider: StoreDataProvider<T>) : AutoCloseable {
    private val changes = mutableMapOf<String, Record>()
    protected fun data(): T = dataProvider.data()

    abstract fun <T> read(type: KClass<T>): T where T : Record
    protected abstract fun save(change: Map.Entry<String, Record>)
    override fun close() { for (ch in changes) save(ch) }
    fun <T> write(value: T) where T : Record { changes[getAnnotation(value::class).name] = value }

    companion object {
        @JvmStatic
        protected fun getAnnotation(type: KClass<*>): DataStoreRecord {
            assert(type.hasAnnotation<DataStoreRecord>()) { "${type.jvmName} is not a DataStoreRecord!" }
            val annotation = type.findAnnotation<DataStoreRecord>()!!
            assert(type.isData && type.java.isRecord) { "${annotation.name} is not a jvm record class!" }
            return annotation
        }
    }
}
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DataStoreRecord(val name: String)
fun interface StoreDataProvider<T> { fun data(): T }

private fun taggableDataProvider(obj: Taggable) = StoreDataProvider {
    obj.getTag(TAG_STORE_ROOT) as NBTCompound? ?: NBTCompound.EMPTY
}

fun interface TagStoreCompanion { fun defaultFunc(missing: String): NBT }
class TagStore(private val obj: Taggable) : DataStore<NBTCompound>(taggableDataProvider(obj)) {
    override fun <T> read(type: KClass<T>): T where T : Record {
        val name = getAnnotation(type).name
        var compound = data().getCompound(name) ?: NBTCompound.EMPTY
        val updatedCompound = compound.toMutableCompound()
        for (c in type.java.recordComponents) if (compound[c.name] == null) {
            updatedCompound[c.name] = (type.companionObjectInstance as TagStoreCompanion).defaultFunc(c.name)
        }
        compound = updatedCompound.toCompound()
        return Tag.View(type.java).read(compound) ?: throw NullPointerException("Couldn't read session data `$name`!")
    }
    override fun save(change: Map.Entry<String, Record>) {
        val newNBT = NBTCompound.EMPTY.toMutableCompound()
        val rootNBT = data().toMutableCompound()
        Tag.View(change.value.javaClass).write(newNBT, change.value)
        rootNBT[change.key] = newNBT.toCompound()
        obj.setTag(TAG_STORE_ROOT, rootNBT.toCompound())
    }

    companion object {
        fun <T> tag(type: KClass<T>): Tag<T> where T : Record {
            return Tag.Structure(getAnnotation(type).name, type.java).path(TAG_STORE_ROOT.key)
        }
    }
}

interface PersistentStoreCompanion { val default: Record? }
class PersistentStore<T : PersistentData>(provider: StoreDataProvider<T>) : DataStore<T>(provider) {
    override fun <T : Record> read(type: KClass<T>): T {
        val annotation = getAnnotation(type)
        val bytes: ByteArray
        try { bytes = data()[annotation.name] } catch (e: Exception) {
            val default = (type.companionObjectInstance as PersistentStoreCompanion).default ?: throw e
            return default as T
        }
        return cborReadByteArray(type.java, bytes)
    }

    override fun save(change: Map.Entry<String, Record>) {
        data()[change.key] = cborByteArray(change.value)
    }
}
interface PersistentData {
    operator fun get(section: String): ByteArray
    operator fun set(section: String, bytes: ByteArray)
}
