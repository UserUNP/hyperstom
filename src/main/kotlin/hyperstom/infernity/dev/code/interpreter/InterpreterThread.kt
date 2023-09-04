package hyperstom.infernity.dev.code.interpreter

// i have a better idea rather than having an actual thread
// using a thread pool executor (with 3 threads or something) for an single *active* plot
// by an active plot i mean there must be at least one player
// but that wouldn't allow for cool stuff like game events

class InterpreterThread : Runnable {
    private val thread: Thread = Thread(this)

    fun start() = thread.start()
    fun end() = thread.join()

    override fun run() {
    }
}
