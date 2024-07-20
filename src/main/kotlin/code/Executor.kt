@file:Suppress("UnstableApiUsage")

package ma.userunp.hyperstom.code

import kotlinx.coroutines.*
import net.kyori.adventure.audience.Audience
import net.minestom.server.event.trait.InstanceEvent
import ma.userunp.hyperstom.Named
import net.minestom.server.listener.manager.PacketListenerManager
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

interface CodeInvokable {
    val id: String
}

interface CodeLabelType<T : CodeInvokable> : Named {
    fun get(name: String): T
}

private inline fun <T : CodeInvokable> implLabelType(n: String, crossinline g: (String) -> T) = object : CodeLabelType<T> {
    override val name = n
    override fun get(name: String) = g(name)
}

data class CodeLabel<T : CodeInvokable>(val type: CodeLabelType<T>, val label: String)

private fun <T : CodeInvokable> CodeLabelType<T>.label(n: String) = CodeLabel(this, n)

/* TODO:
find out how to implement a minestom scheduler utilizing kotlin coroutines
because trying to run asynchronously outside the event caller's tick thread or scheduler isn't the best idea
*/

data class InvokeContext(
    val invokable: CodeInvokable,
    val msEvent: InstanceEvent,
    val instList: InstList,
    val label: CodeLabel<*>,
)

interface CodeInvoker {  //TODO: a debug invoker for debugging, and maybe misc invokers for fun
    val scope: CoroutineScope
    fun exec(ctx: InvokeContext, labelResolver: CodeLabelResolver)
}

class RuntimeInvoker(private val id: UUID) : CodeInvoker {
    override val scope = CoroutineScope(Dispatchers.Default)
    override fun exec(ctx: InvokeContext, labelResolver: CodeLabelResolver) {
        scope.launch(CoroutineName("$id:${ctx.invokable.id}")) {
            //TODO: customizable code executor or something
            val executor = CodeExecutorImpl(ctx, 0, labelResolver)
            try {
                for (inst in executor) {
                    val targetClass = inst.target.targetClass()
                    if (!targetClass.worksWith(inst.type.targetClass)) throw RuntimeException(
                        "Cannot use target with class $targetClass against the instruction ${inst.type.name}!"
                    )
                    try {
                        inst.type.exec(InstContext(
                            executor, ctx.msEvent.instance
                        ) { inst.target.get(ctx.msEvent) })
                    } catch (e: Exception) { throw RuntimeException("Error whilst executing ${inst.type.name}", e) }
                }
            } catch(e: Exception) { throw RuntimeException("Runtime exception.", e) }
        }
    }
}

interface EventDataProcessor<T : InstanceEvent, out S : Any> : Named {
    val eventType: KClass<T>
    fun get(msEvent: T): S
}

fun <T : InstanceEvent, S : InstanceEvent, U : Any> EventDataProcessor<S, U>.get(msEvent: T) =
    this.get(eventType.safeCast(msEvent) ?:
    throw RuntimeException("Event type ${eventType.simpleName} is not compatible with ${msEvent::class.simpleName}!"))

typealias EventTarget<T> = EventDataProcessor<T, Set<Audience>>
typealias EventVal<T, S> = EventDataProcessor<T, CodeVal<S>>

enum class CodeEvent(
    //TODO: use these in dev mode to let the user access these type of code values
    val values: Set<EventVal<*, *>> = setOf(),
    val targets: Set<EventTarget<*>> = setOf(),
) : CodeInvokable {
    // world
    INIT,
    // entity
    CHAT(targets = setOf(TargetDefault), values = setOf(EventValChatMsg)),
    ;
    override val id = name
}

class CodeProcess(override val id: String) : CodeInvokable
//TODO: maybe process parameters once i implement the param val type

object LabelEventType : CodeLabelType<CodeEvent> by implLabelType("E", CodeEvent::valueOf)
fun eventLabel(e: CodeEvent) = LabelEventType.label(e.id)

object LabelDataType : CodeLabelType<CodeProcess> by implLabelType("D", ::CodeProcess)
fun dataLabel(n: String) = LabelDataType.label(n)

//TODO: idfk how this would work, MAYBE this needs rethinking
object LabelScopedType : CodeLabelType<Nothing> by implLabelType("S", { TODO("Idk how to turn a scoped label into an invokable!") })
fun scopedLabel(n: String) = LabelScopedType.label(n)

private typealias CodeVarMap = MutableMap<String, CodeVal<*>>

class ExecFrame<T : CodeInvokable>(
    val label: CodeLabel<T>,
    val instList: InstList,
    var instIndex: Int,
    val vars: CodeVarMap = mutableMapOf(),
    var previous: ExecFrame<*>? = null,
)

fun interface CodeLabelResolver {
    fun resolveLabel(label: CodeLabel<*>): InstList
}

fun interface CodeVarResolver {
    fun resolveVar(name: String): CodeVal<*>
}

interface CodeArgResolver {
    fun <T : Any> resolveRuntimeArg(c: CodeVal<T>): CodeVal<*>
    fun <T : Any> argCodeVal(p: ParamNode<T>): List<CodeVal<T>>
    fun <T : Any> arg(p: ParamNode<T>): List<T>
}

interface CodeFlowControl {
    var frame: ExecFrame<*>
    fun jumpTo(label: CodeLabel<*>, startIndex: Int)
}

interface CodeExecutor : CodeVarResolver, CodeArgResolver, CodeFlowControl, Iterable<CodeInst>, Iterator<CodeInst>

private class CodeExecutorImpl(
    ctx: InvokeContext,
    startIndex: Int,
    private val labelResolver: CodeLabelResolver,
) : CodeExecutor {
    override var frame = ExecFrame(ctx.label, ctx.instList, startIndex)
    private lateinit var currInst: CodeInst
    private val msEvent = ctx.msEvent

    override operator fun next(): CodeInst {
        currInst = frame.instList[frame.instIndex++]
        return currInst
    }

    override operator fun hasNext(): Boolean {
        val hasNext = frame.instIndex <= frame.instList.size-1
        if (!hasNext) frame.previous?.let {
            frame = it
            return@hasNext hasNext()
        }
        return hasNext
    }

    override operator fun iterator() = this

    override fun jumpTo(label: CodeLabel<*>, startIndex: Int) {
        val instList = labelResolver.resolveLabel(label)
        frame = ExecFrame(label, instList, startIndex, previous = frame)
    }

    override fun resolveVar(name: String) = frame.vars[name]
        ?: (if (frame.label.type == LabelScopedType) frame.previous?.let { it.vars[name] } else null)
        ?: throw RuntimeException("No such var! $name")

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> resolveRuntimeArg(c: CodeVal<T>) = when (c.type) {
        ValTypeVar -> resolveVar(c.value as String)
        ValTypeEventVal -> (c.value as EventVal<*, *>).get(msEvent)
        else -> throw RuntimeException("${c.type} is not a runtime code val type!")
    }

    override fun <T : Any> argCodeVal(p: ParamNode<T>): List<CodeVal<T>> {
        val vals = (currInst.args[p.name] ?: throw RuntimeException("Unknown param! ${p.name}"))
        return vals.toMutableList().apply { if (any { it.type.isRuntime() }) {
            replaceAll { if (it.type.isRuntime()) resolveRuntimeArg(it) else it }
            p.compute(ParamNodeResult(0, size, this, mutableMapOf(p.name to this)))
        } } as List<CodeVal<T>> //TODO: find a better way to do this
    }

    override fun <T : Any> arg(p: ParamNode<T>) = argCodeVal(p).map(CodeVal<T>::value)
}
