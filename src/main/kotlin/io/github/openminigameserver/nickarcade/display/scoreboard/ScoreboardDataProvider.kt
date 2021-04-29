package io.github.openminigameserver.nickarcade.display.scoreboard

import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import net.kyori.adventure.text.Component
import org.bukkit.scoreboard.Team

interface ScoreboardDataProvider {
    suspend fun provideTeamConfiguration(player: ArcadePlayer, team: Team): Boolean = false

    suspend fun providePrefix(player: ArcadePlayer): Component? = null

    suspend fun provideSuffix(player: ArcadePlayer): Component? = null

    suspend fun provideSideBar(player: ArcadePlayer): SidebarData? = null
}