package io.github.openminigameserver.nickarcade.display.managers

import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.display.scoreboard.ScoreboardData
import io.github.openminigameserver.nickarcade.display.scoreboard.ScoreboardDataProvider
import io.github.openminigameserver.nickarcade.display.scoreboard.impl.DefaultScoreboardDataProvider
import org.bukkit.scoreboard.Team

object ScoreboardDataProviderManager {

    private val providers = mutableListOf<ScoreboardDataProvider>(
        DefaultScoreboardDataProvider,
    )

    fun registerProvider(provider: ScoreboardDataProvider) {
        providers.add(0, provider)
    }

    suspend fun computeData(target: ArcadePlayer, viewer: ArcadePlayer, team: Team, excludeScoreboard: Boolean): ScoreboardData {
        val providedTeamModification = providers.firstOrNull { it.provideTeamConfiguration(target, viewer, team) } != null
        val prefix = providers.takeUnless { providedTeamModification }?.mapNotNull { it.providePrefix(target, viewer) }?.firstOrNull()
        val suffix = providers.takeUnless { providedTeamModification }?.mapNotNull { it.provideSuffix(target, viewer) }?.firstOrNull()
        val sideBar = providers.takeUnless { providedTeamModification }?.takeIf { !excludeScoreboard }?.mapNotNull { it.provideSideBar(target) }?.firstOrNull()
        return ScoreboardData(prefix, suffix, sideBar, providedTeamModification)
    }
}