package io.github.openminigameserver.nickarcade.display.scoreboard

import net.kyori.adventure.text.Component

data class ScoreboardData(
    val prefix: Component? = null,
    val suffix: Component? = null,
    val sideBar: SidebarData?,
    var providedTeamData: Boolean = false
)