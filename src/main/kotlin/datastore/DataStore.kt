@file:Suppress("UnstableApiUsage", "UNCHECKED_CAST")

package dev.bedcrab.hyperstom.datastore

import dev.bedcrab.hyperstom.cborByteArray
import dev.bedcrab.hyperstom.cborReadByteArray
import net.minestom.server.tag.Tag
import net.minestom.server.tag.TagReadable
import net.minestom.server.tag.TagWritable
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmName

private val TAG_STORE_ROOT = Tag.NBT("HYPERSTOM")

abstract class DataStore<T>(private val dataProvider: StoreDataProvider<T>) : AutoCloseable {
    private val changes = mutableMapOf<String, Any>()
    protected fun data(): T = dataProvider.data()

    abstract fun <T : Any> read(type: KClass<T>): T
    protected abstract fun save(change: Map.Entry<String, Any>)
    override fun close() { for (ch in changes) save(ch) }
    fun <T : Any> write(value: T) { changes[getAnnotation(value::class).name] = value }

    companion object {
        @JvmStatic
        protected fun getAnnotation(type: KClass<*>): DataStoreRecord {
            assert(type.hasAnnotation<DataStoreRecord>()) { "${type.jvmName} is not a DataStoreRecord!" }
            val annotation = type.findAnnotation<DataStoreRecord>()!!
            return annotation
        }
    }
}
@Target(AnnotationTarget.CLASS) @Retention(AnnotationRetention.RUNTIME)
annotation class DataStoreRecord(val name: String)
fun interface StoreDataProvider<T> { fun data(): T }

fun interface TagStoreCompanion { fun defaultFunc(missing: String): NBT }
class TagStore(
    private val obj: TagReadable,
    private val nbtWrite: (nbt: NBTCompound) -> Unit = {
        if (obj !is TagWritable) throw RuntimeException("Cannot modify an immutable object!")
        obj.setTag(TAG_STORE_ROOT, it)
    }
) : DataStore<NBTCompound>(StoreDataProvider { obj.getTag(TAG_STORE_ROOT) as NBTCompound? ?: NBTCompound.EMPTY }) {
    override fun <T : Any> read(type: KClass<T>): T {
        assert(type.java.isRecord) { "Cannot read a non-record value from a tag!" }
        val recordType = type.java as Class<Record>
        val name = getAnnotation(type).name
        var compound = data().getCompound(name) ?: NBTCompound.EMPTY
        val updatedCompound = compound.toMutableCompound()
        for (c in type.java.recordComponents) if (compound[c.name] == null) {
            updatedCompound[c.name] = (type.companionObjectInstance as TagStoreCompanion).defaultFunc(c.name)
        }
        compound = updatedCompound.toCompound()
        return Tag.View(recordType).read(compound) as T? ?: throw NullPointerException("Couldn't read tag $name!")
    }
    override fun save(change: Map.Entry<String, Any>) {
        assert(change.value is Record) { "Cannot write a non-record value to a tag!" }
        val value = change.value as Record
        val newNBT = NBTCompound.EMPTY.toMutableCompound()
        val rootNBT = data().toMutableCompound()
        Tag.View(value.javaClass).write(newNBT, value)
        rootNBT[change.key] = newNBT.toCompound()
        nbtWrite(rootNBT.toCompound())
    }

    companion object {
        fun <T> tag(type: KClass<T>): Tag<T> where T : Record {
            return Tag.Structure(getAnnotation(type).name, type.java).path(TAG_STORE_ROOT.key)
        }
    }
}



interface PersistentStoreCompanion { val default: Any? }
class PersistentStore<T : PersistentData>(provider: StoreDataProvider<T>) : DataStore<T>(provider) {
    override fun <T : Any> read(type: KClass<T>): T {
        val annotation = getAnnotation(type)
        val bytes: ByteArray
        try { bytes = data()[annotation.name] } catch (e: Exception) {
            val default = (type.companionObjectInstance as PersistentStoreCompanion).default ?: throw e
            return default as T
        }
        return cborReadByteArray(type.java, bytes)
    }

    override fun save(change: Map.Entry<String, Any>) {
        data()[change.key] = cborByteArray(change.value)
    }
}
interface PersistentData {
    operator fun get(section: String): ByteArray
    operator fun set(section: String, bytes: ByteArray)
}
