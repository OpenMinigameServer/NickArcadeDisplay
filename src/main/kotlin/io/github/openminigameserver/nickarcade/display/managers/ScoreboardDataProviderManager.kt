package io.github.openminigameserver.nickarcade.display.managers

import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.display.scoreboard.ScoreboardData
import io.github.openminigameserver.nickarcade.display.scoreboard.ScoreboardDataProvider
import io.github.openminigameserver.nickarcade.display.scoreboard.impl.DefaultScoreboardDataProvider

object ScoreboardDataProviderManager {

    private val providers = mutableListOf<ScoreboardDataProvider>(
        DefaultScoreboardDataProvider,
    )

    fun registerProvider(provider: ScoreboardDataProvider) {
        providers.add(0, provider)
    }

    suspend fun computeData(player: ArcadePlayer, excludeScoreboard: Boolean): ScoreboardData {
        val prefix = providers.mapNotNull { it.providePrefix(player) }.firstOrNull()
        val suffix = providers.mapNotNull { it.provideSuffix(player) }.firstOrNull()
        val sideBar = providers.takeIf { !excludeScoreboard }?.mapNotNull { it.provideSideBar(player) }?.firstOrNull()
        return ScoreboardData(prefix, suffix, sideBar)
    }
}