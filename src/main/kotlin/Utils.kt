package ma.userunp.hyperstom

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.kyori.adventure.text.minimessage.MiniMessage

val MM = MiniMessage.miniMessage()

fun getResource(path: String) = object {}::class.java.classLoader.getResource(path)
    ?: throw NullPointerException("\"$path\" does not exist as a resource")

fun bytesToHumanReadable(bytes: Int) = when {
    bytes >= 1 shl 30 -> "%.3f GB".format(bytes.toFloat() / (1 shl 30))
    bytes >= 1 shl 20 -> "%.3f MB".format(bytes.toFloat() / (1 shl 20))
    bytes >= 1 shl 10 -> "%.3f kB".format(bytes.toFloat() / (1 shl 10))
    else -> "$bytes bytes"
}

fun <K, V> linkedHashMap(vararg pairs: Pair<K, V>): Object2ObjectLinkedOpenHashMap<K, V> {
    val map = Object2ObjectLinkedOpenHashMap<K, V>(pairs.size)
    for (p in pairs) map[p.first] = p.second
    return map
}

fun <K, V> mutableMap(vararg pairs: Pair<K, V>): Object2ObjectOpenHashMap<K, V> {
    val map = Object2ObjectOpenHashMap<K, V>(pairs.size)
    for (p in pairs) map[p.first] = p.second
    return map
}
