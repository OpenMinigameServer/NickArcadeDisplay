package io.github.openminigameserver.nickarcade.display.scoreboard.impl

import io.github.openminigameserver.hypixelapi.models.HypixelPackageRank
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.display.scoreboard.ScoreboardDataProvider
import io.github.openminigameserver.nickarcade.display.scoreboard.SidebarData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

object DefaultScoreboardDataProvider : ScoreboardDataProvider {
    override suspend fun provideSideBar(player: ArcadePlayer): SidebarData {
        val title = text("NICKARCADE", NamedTextColor.WHITE, TextDecoration.BOLD)
        return SidebarData(
            title, mutableListOf<Component>().apply {
                add(empty())
                var prefix = text(player.computeEffectivePrefix(true)?.trim()?.replace("[", "")?.replace("]", "") ?: "")
                if (player.effectiveRank <= HypixelPackageRank.NORMAL) {
                    prefix = text("Default", NamedTextColor.GRAY)
                }
                add(text("Rank: ").append(prefix))
                add(text("Level: ").append(text(player.actualNetworkLevel.toInt(), NamedTextColor.GREEN)))
            }
        )
    }

    override suspend fun providePrefix(target: ArcadePlayer, viewer: ArcadePlayer): Component {
        return text(target.effectivePrefix)
    }

    override suspend fun provideSuffix(target: ArcadePlayer, viewer: ArcadePlayer): Component? {
        if (target.isFloodgatePlayer) {
            return text(" [BEDROCK]", NamedTextColor.DARK_AQUA)
        }

        return null
    }
}