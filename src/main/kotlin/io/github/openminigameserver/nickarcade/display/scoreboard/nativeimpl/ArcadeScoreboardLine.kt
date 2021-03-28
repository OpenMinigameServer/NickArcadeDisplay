package io.github.openminigameserver.nickarcade.display.scoreboard.nativeimpl

import io.github.openminigameserver.nickarcade.display.scoreboard.nativeimpl.ArcadeScoreboard.Companion.maxScoreNumber
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor


data class ArcadeScoreboardLine(val scoreboard: ArcadeScoreboard, val index: Int) {
    companion object {
        val colors = ChatColor.values().map { "§${it.char}" }
    }

    private val score = scoreboard.objective.getScore(colors[index]).apply { score = maxScoreNumber - index }

    private val team =
        scoreboard.scoreboard.getTeam("score$index") ?: scoreboard.scoreboard.registerNewTeam("score$index")

    init {
        team.addEntry(score.entry)
    }

    fun setValue(message: Component) {
        team.prefix(message)
    }

    fun remove() {
        scoreboard.scoreboard.resetScores(score.entry)
    }
}