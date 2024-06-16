@file:Suppress("UnstableApiUsage", "UNCHECKED_CAST")

package userunp.hyperstom.datastore

import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.CompoundBinaryTag
import userunp.hyperstom.cborByteArray
import userunp.hyperstom.cborReadByteArray
import net.minestom.server.tag.Tag
import net.minestom.server.tag.TagReadable
import net.minestom.server.tag.TagWritable
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.findAnnotation
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
            val annotation = type.findAnnotation<DataStoreRecord>()
                ?: throw NullPointerException("${type.jvmName} is not a DataStoreRecord!")
            return annotation
        }
    }
}
@Target(AnnotationTarget.CLASS) @Retention(AnnotationRetention.RUNTIME)
annotation class DataStoreRecord(val name: String)
fun interface StoreDataProvider<T> { fun data(): T }

fun interface TagStoreCompanion { fun defaultFunc(missing: String): BinaryTag }
class TagStore(
    private val obj: TagReadable,
    private val nbtWrite: (nbt: CompoundBinaryTag) -> Unit = {
        if (obj !is TagWritable) throw RuntimeException("Cannot modify an immutable object!")
        obj.setTag(TAG_STORE_ROOT, it)
    }
) : DataStore<CompoundBinaryTag>(StoreDataProvider { obj.getTag(TAG_STORE_ROOT) as CompoundBinaryTag? ?: CompoundBinaryTag.empty() }) {
    override fun <T : Any> read(type: KClass<T>): T {
        assert(type.java.isRecord) { "Cannot read a non-record value from a tag!" }
        val recordType = type.java as Class<Record>
        val name = getAnnotation(type).name
        val compound = CompoundBinaryTag.from(data().getCompound(name).let { compound -> type.java.recordComponents.associate {
            it.name to (compound[it.name] ?: (type.companionObjectInstance as TagStoreCompanion).defaultFunc(it.name))
        } })
        return Tag.View(recordType).read(compound) as T? ?: throw NullPointerException("Couldn't read tag $name!")
    }

    override fun save(change: Map.Entry<String, Any>) {
        assert(change.value is Record) { "Cannot write a tag as a non-record value!" }
        val value = change.value as Record
        val newNBT = CompoundBinaryTag.builder()
        Tag.View(value.javaClass).write(newNBT, value)
        nbtWrite(data().put(change.key, newNBT.build()))
    }

    companion object {
        fun <T : Record> tag(type: KClass<T>): Tag<T> {
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
