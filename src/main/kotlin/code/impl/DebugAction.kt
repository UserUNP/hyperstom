package dev.bedcrab.hyperstom.code.impl

import dev.bedcrab.hyperstom.code.InstFunction
import net.kyori.adventure.text.Component

val printInstructions = InstFunction {
    it.instance.sendMessage { Component.text(it.list.toString()) }
}
