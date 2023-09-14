package hyperstom.infernity.dev

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

val gsonSerializer = GsonComponentSerializer.gson()

fun getResource(path: String) = object {}::class.java.classLoader.getResourceAsStream(path) ?: throw NullPointerException("\"$path\" does not exist as a resource")
