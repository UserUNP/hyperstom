package userunp.hyperstom

class HSException(cause: Throwable, msg: String? = null): Exception(cause) {
    override val message = msg ?: if (cause.message != null) "------------" else "(unknown reason)"
    val msg = MM.deserialize(StringBuilder().apply {
        appendLine("<dark_red>[ERROR] <red>$message")
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
class CommandException(msg: String) : Exception(msg)
class DataStoreException(msg: String) : Exception(msg)
class WorldIOException(msg: String) : Exception(msg)
