package dev.bedcrab.hyperstom.code

import dev.bedcrab.hyperstom.code.impl.assignVar
import dev.bedcrab.hyperstom.GSON_SERIALIZER
import dev.bedcrab.hyperstom.cborDeserialize
import dev.bedcrab.hyperstom.code.impl.bruhMoner
import dev.bedcrab.hyperstom.code.impl.printInstructions
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.NBTType

typealias InstList = MutableList<Instruction>
@Serializable
data class Instruction(val props: InstProperties) { // TODO: add args (and params)
    operator fun invoke(instance: Instance) = props.exec(getCtx(instance))
    private fun getCtx(instance: Instance) = ExecContext(instance, this)

    /**
     * Instruction visual
     */
    @Serializable
    data class Visual(val l1: String, val l2: String, val l3: String, val l4: String) {
        val block = INST_VIS_MS_BLOCK.withNbt(
            NBT.Compound(mapOf(
                "is_waxed" to NBT.Boolean(true),
                "front_text" to NBT.Compound(mapOf(
                    "color" to NBT.String("black"),
                    "has_glowing_text" to NBT.Boolean(false),
                    "messages" to NBT.List(
                        NBTType.TAG_String,
                        NBT.String(GSON_SERIALIZER.serialize(Component.text(l1).decorate(TextDecoration.BOLD))),
                        NBT.String(GSON_SERIALIZER.serialize(Component.text(l2))),
                        NBT.String(GSON_SERIALIZER.serialize(Component.text(l3))),
                        NBT.String(GSON_SERIALIZER.serialize(Component.text(l4))),
                    ),
                )),
            )))
        companion object {
            fun from(block: Block): Visual {
                val nbt = block.nbt() ?: throw NullPointerException("Failed to get block nbt!")
                return cborDeserialize<Visual>(nbt)
            }
        }
    }
}

enum class InstProperties(val fullName: String, val exec: InstFunction) {
    `bruh moner`("bruh moner", bruhMoner),
    INST_STACK("Print Instructions", printInstructions),
    `=`("Assign Variable", assignVar),
    ;
    val label = name.replace("_", " ")
}
