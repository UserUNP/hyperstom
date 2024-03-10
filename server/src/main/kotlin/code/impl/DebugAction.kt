package dev.bedcrab.hyperstom.code.impl

import dev.bedcrab.hyperstom.code.InstFunction
import net.kyori.adventure.text.Component

val bruhMoner = InstFunction {
    it.instance.sendMessage { Component.text("bruh moner") }
}

val printInstructions = InstFunction {
    TODO()
}
