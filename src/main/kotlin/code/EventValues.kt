@file:Suppress("UnstableApiUsage")

package ma.userunp.hyperstom.code

import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.event.trait.InstanceEvent
import kotlin.reflect.KClass

private fun <T : InstanceEvent, S : Any> implEventVal(n: String, e: KClass<T>, t: CodeValType<S>, g: (T) -> S) = object : EventVal<T, S> {
    override val name = n
    override val eventType = e
    override fun get(msEvent: T) = t.get(g(msEvent))
}

object EventValChatMsg : EventVal<PlayerChatEvent, String> by implEventVal("MSG",
    PlayerChatEvent::class, ValTypeStr,
    { it.message },
)
