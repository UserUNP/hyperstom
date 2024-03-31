package dev.bedcrab.hyperstom.code

import dev.bedcrab.hyperstom.code.impl.parametersTest
import dev.bedcrab.hyperstom.code.impl.printInstructions
import kotlinx.serialization.Serializable
import net.minestom.server.instance.Instance

typealias InstList = MutableList<Instruction>

@Serializable data class Instruction(val props: InstProperties) { // TODO: add args (and params)
    operator fun invoke(instance: Instance, list: InstList) = props.exec(ExecContext(list, this, instance))
}

enum class InstProperties(val exec: InstFunction, val params: Map<String, Parameter<*>> = mapOf()) {
    PRINT_INSTRUCTIONS(printInstructions),
    PARAMETERS_TEST(parametersTest, mapOf(param("first", STR_VALUE_TYPE))),
    ;
}

// short names so intellij doesn't bloat the line with parameter hints
private fun <T> param(n: String, t: CodeValueType<T>, o: Boolean = false, p: Boolean = false)
    = n to Parameter(n, t, o, p)
data class Parameter<T>(val name: String, val type: CodeValueType<T>, val optional: Boolean, val plural: Boolean) {
}
