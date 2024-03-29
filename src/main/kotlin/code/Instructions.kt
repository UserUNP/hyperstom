package dev.bedcrab.hyperstom.code

import dev.bedcrab.hyperstom.code.impl.assignVar
import dev.bedcrab.hyperstom.code.impl.parametersTest
import dev.bedcrab.hyperstom.code.impl.printInstructions
import kotlinx.serialization.Serializable
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.NBTType

typealias InstList = MutableList<Instruction>

/**
 * Instruction visual
 */
typealias InstVisual<T> = () -> T

@Serializable class MinestomInstVisual(
    private val l1: String,
    private val l2: String,
    private val l3: String,
    private val l4: String,
) : InstVisual<Block> {
    override fun invoke() = INST_VIS_MS_BLOCK.withNbt(NBT.Compound(mapOf(
        "is_waxed" to NBT.Boolean(true), "front_text" to NBT.Compound(mapOf(
            "color" to NBT.String("black"),
            "has_glowing_text" to NBT.Boolean(false),
            "messages" to NBT.List(NBTType.TAG_String,
                NBT.String("""{"text": "$l1", "bold": true}"""),
                NBT.String("""{"text": "$l2"}"""),
                NBT.String("""{"text": "$l3"}"""),
                NBT.String("""{"text": "$l4"}"""),
            ),
        )),
    )))
}

class Parameters(private val raw: Map<String, CodeValueType<*>>) {
    constructor(vararg pairs: Pair<String, CodeValueType<*>>) : this(mapOf(*pairs))
    fun single(name: String) = raw[name]
}

@Serializable data class Instruction(val props: InstProperties) { // TODO: add args (and params)
    operator fun invoke(instance: Instance, list: InstList) = props.exec(ExecContext(list, this, instance))
}

enum class InstProperties(val fullName: String, val exec: InstFunction, val params: Parameters = Parameters()) {
    PRINT_INSTRUCTIONS("Print Instructions", printInstructions),
    PARAMETERS_TEST("Parameters Test", parametersTest, Parameters("first" to STR_VALUE_TYPE)),
    `=`("Assign Variable", assignVar),
    ;
    val label = name.replace("_", " ")
}
