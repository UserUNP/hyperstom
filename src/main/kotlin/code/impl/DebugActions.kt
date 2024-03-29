package dev.bedcrab.hyperstom.code.impl

import dev.bedcrab.hyperstom.code.InstFunction
import net.kyori.adventure.text.Component

val printInstructions: InstFunction = {
    instance.sendMessage { Component.text(instList.toString()) }
}

val parametersTest: InstFunction = {
}
