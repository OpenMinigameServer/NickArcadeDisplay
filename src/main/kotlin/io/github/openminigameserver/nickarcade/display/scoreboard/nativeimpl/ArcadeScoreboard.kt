package io.github.openminigameserver.nickarcade.display.scoreboard.nativeimpl

import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.display.scoreboard.SidebarData
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective

class ArcadeScoreboard(private val arcadePlayer: ArcadePlayer, sidebarData: SidebarData) {

    companion object {
        const val maxScoreNumber = 15
        const val objectiveName = "scoreboard"
    }

    var sidebarData = sidebarData
        set(value) {
            updateLines(value)
            field = value
        }

    val scoreboard = arcadePlayer.player!!.scoreboard

    val objective = registerObjective()

    private fun registerObjective(): Objective {
        val objective = scoreboard.getObjective(objectiveName) ?: scoreboard.registerNewObjective(
            objectiveName,
            "dummy",
            sidebarData.title
        )

        return objective
    }

    private fun updateLines(value: SidebarData) {
        objective.displayName(sidebarData.title)
        updateLineCount(value)

        val newLines = value.lines
        lines.forEachIndexed { i, line ->
            line.setValue(newLines[i])
        }

    }

    private fun updateLineCount(value: SidebarData) {
        val oldData = sidebarData
        val oldSize = oldData.lines.size
        val newSize = value.lines.size
        if (newSize > oldSize) {
            for (i in oldSize until newSize) {
                lines.add(createScoreboardLine(i))
            }
        } else if (oldSize > newSize) {
            repeat(oldSize - newSize) {
                lines.lastOrNull()?.remove()
                lines.removeLast()
            }
        }
    }

    val lines: MutableList<ArcadeScoreboardLine> = createScoreboardLines(sidebarData)

    private fun createScoreboardLines(sidebarData: SidebarData) =
        (0 until sidebarData.lines.size.coerceAtMost(maxScoreNumber)).map {
            createScoreboardLine(it)
        }.toMutableList()

    private fun createScoreboardLine(it: Int) = ArcadeScoreboardLine(this, it)

    fun show() {
        objective.displaySlot = DisplaySlot.SIDEBAR
    }

}