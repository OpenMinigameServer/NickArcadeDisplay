package io.github.openminigameserver.nickarcade.display.managers

import io.github.openminigameserver.nickarcade.core.data.sender.player.extra.RuntimeExtraDataTag
import io.github.openminigameserver.nickarcade.core.manager.getArcadeSender
import io.github.openminigameserver.nickarcade.display.scoreboard.ScoreboardData
import io.github.openminigameserver.nickarcade.display.scoreboard.nativeimpl.ArcadeScoreboard
import io.github.openminigameserver.nickarcade.plugin.extensions.async
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getOnlinePlayers
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team


object ScoreboardManager {

    private val scoreboardTag = RuntimeExtraDataTag.of<Scoreboard>("scoreboard")
    private val sidebarTag = RuntimeExtraDataTag.of<ArcadeScoreboard>("sidebar")
    private val bossBarTag = RuntimeExtraDataTag.of<BossBar>("bossbar")

    suspend fun refreshScoreboard(viewer: Player) {
        val viewerPlayer = async { viewer.getArcadeSender() }
        val playerScoreboard = viewerPlayer[scoreboardTag]
            ?: Bukkit.getScoreboardManager().newScoreboard.also {
                viewer.scoreboard = it; viewerPlayer[scoreboardTag] = it
            }

        getOnlinePlayers().forEach { target ->
            val targetPlayer = async { target.getArcadeSender() }
            val playerName = targetPlayer.displayName.take(16)

            val team = playerScoreboard.getTeam(playerName) ?: playerScoreboard.registerNewTeam(playerName)

            val scoreData = ScoreboardDataProviderManager.computeData(
                targetPlayer,
                viewerPlayer,
                team,
                targetPlayer != viewerPlayer
            )
            applyScoreboardTeam(team, scoreData, playerName)
            val sidebarData = scoreData.sideBar
            if (sidebarData != null) {
                val sidebar = targetPlayer[sidebarTag] ?: ArcadeScoreboard(targetPlayer, sidebarData)
                sidebar.show()
                sidebar.sidebarData = sidebarData
                targetPlayer[sidebarTag] = sidebar
            }
        }
    }

    private fun applyScoreboardTeam(
        team: Team,
        scoreData: ScoreboardData,
        playerName: String
    ) {
        if (!scoreData.providedTeamData) {
            team.prefix(scoreData.prefix ?: empty())
            team.suffix(
                scoreData.suffix?.replaceText(
                    TextReplacementConfig.builder().match("\\s+\$").replacement("").build()
                )?.let { text(' ').append(it) } ?: empty())
            //Compute team color
            scoreData.prefix?.let { prefix ->
                val prefixComponent = LegacyComponentSerializer.legacySection()
                    .deserialize(LegacyComponentSerializer.legacySection().serialize(prefix))
                val teamColor = prefixComponent.color() ?: prefixComponent.children().flatMap { it.children() + it }
                    .lastOrNull { it.color() != null }?.color()
                teamColor?.let { NamedTextColor.nearestTo(it) }?.also {
                    team.color(it)
                }
            }
        }
        team.addEntry(playerName)
    }
}