package hyperstom.infernity.dev.code.interpreter

class InterpreterThread : Runnable {
    private val thread: Thread = Thread(this)

    fun start() = thread.start()
    fun end() = thread.join()

    override fun run() {
    }
}
