package ma.userunp.hyperstom

import net.kyori.adventure.text.minimessage.MiniMessage

val MM = MiniMessage.miniMessage()

fun getResource(path: String) = object {}::class.java.classLoader.getResource(path)
    ?: throw NullPointerException("\"$path\" does not exist as a resource")
