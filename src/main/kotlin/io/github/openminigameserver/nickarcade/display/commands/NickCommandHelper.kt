package io.github.openminigameserver.nickarcade.display.commands

import io.github.openminigameserver.nickarcade.core.commandManager

object NickCommandHelper {
    fun register() {
        commandManager.parserRegistry.registerNamedParserSupplier("nickrank") {
            NickRankParser()
        }
    }
}