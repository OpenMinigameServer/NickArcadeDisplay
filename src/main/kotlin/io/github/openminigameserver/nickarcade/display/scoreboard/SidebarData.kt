package io.github.openminigameserver.nickarcade.display.scoreboard

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

data class SidebarData(val title: Component, val lines: MutableList<Component>) {
    init {
        lines.apply {
            add(Component.empty())
            add(Component.text("NickArcade", NamedTextColor.YELLOW))
        }
    }
}
