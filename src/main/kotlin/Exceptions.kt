package ma.userunp.hyperstom

import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.MinecraftServer

private val LOGGER = KotlinLogging.logger {}

fun initExceptionHandler() = MinecraftServer.getExceptionManager().setExceptionHandler { LOGGER.throwing(it) }

class HSException(cause: Throwable, msg: String? = null): Exception(cause) {
    override val message = msg ?: if (cause.message != null) "------------" else "(unknown reason)"
    val msg = MM.deserialize(StringBuilder().apply {
        appendLine("<red>[ERROR] <red>$message")
        var throwable: Throwable? = cause
        var i = 1
        while (throwable != null) {
            val color = if (throwable is HSException) "<red>" else "<gray>"
            appendLine("${" ".repeat(i)} <dark_red>* $color${throwable.message}")
            throwable = throwable.cause
            i++
        }
        appendLine("<red>-------------------")
    }.toString())
}

class CodeException(cause: Throwable) : Exception(cause)
class ParamException(msg: String, val mark: Int) : Exception(msg)
class CommandException(msg: String) : Exception(msg)
class WorldIOException(msg: String) : Exception(msg)
