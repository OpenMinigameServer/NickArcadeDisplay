package io.github.openminigameserver.nickarcade.display.commands

import cloud.commandframework.annotations.CommandMethod
import io.github.openminigameserver.hypixelapi.models.HypixelPackageRank
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.core.data.sender.player.extra.PlayerOverrides
import io.github.openminigameserver.nickarcade.display.displayOverrides
import io.github.openminigameserver.nickarcade.display.managers.ProfilesManager
import io.github.openminigameserver.nickarcade.display.nick.DisplayOverrides
import io.github.openminigameserver.nickarcade.display.profiles.setDisplayProfile
import io.github.openminigameserver.nickarcade.plugin.extensions.command
import io.github.openminigameserver.nickarcade.plugin.helper.commands.RequiredRank
import java.util.*
import kotlin.random.Random

object TestCommands {

    internal val ranksRange: EnumSet<HypixelPackageRank> =
        EnumSet.range(HypixelPackageRank.NORMAL, HypixelPackageRank.MVP_PLUS)

    fun randomPlayerOverrides() =
        PlayerOverrides(ranksRange.random(), networkLevel = Random.nextInt(1, 50).toLong(), isLegacyPlayer = false)

    @RequiredRank(HypixelPackageRank.ADMIN)
    @CommandMethod("randomprofile")
    fun testRandomProfile(sender: ArcadePlayer) = command(sender, HypixelPackageRank.ADMIN) {
        val profile = ProfilesManager.profiles.random()
        sender.displayOverrides = DisplayOverrides().apply { overrides = randomPlayerOverrides() }

        sender.player?.setDisplayProfile(profile, true)
    }

    @RequiredRank(HypixelPackageRank.ADMIN)
    @CommandMethod("randomprofile remove")
    fun removeRandomProfile(sender: ArcadePlayer) = command(sender, HypixelPackageRank.ADMIN) {
        sender.displayOverrides = null

        sender.player?.setDisplayProfile(null, true)
    }
}