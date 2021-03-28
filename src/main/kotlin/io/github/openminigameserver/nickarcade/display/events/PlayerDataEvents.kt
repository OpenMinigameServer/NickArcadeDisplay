package io.github.openminigameserver.nickarcade.display.events

import io.github.openminigameserver.hypixelapi.models.HypixelPackageRank
import io.github.openminigameserver.nickarcade.core.events.data.PlayerDataLeaveEvent
import io.github.openminigameserver.nickarcade.core.events.data.PlayerDataReloadEvent
import io.github.openminigameserver.nickarcade.display.displayOverrides
import io.github.openminigameserver.nickarcade.display.managers.ScoreboardManager
import io.github.openminigameserver.nickarcade.display.profiles.setDisplayProfile
import io.github.openminigameserver.nickarcade.plugin.extensions.event
import io.github.openminigameserver.nickarcade.plugin.extensions.launch
import kotlinx.coroutines.delay
import org.bukkit.Bukkit.getOnlinePlayers
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.time.seconds

object PlayerDataEvents {
    fun registerHandlers() {
        launch {
            while (true) {
                for (player in getOnlinePlayers()) {
                    player.let { ScoreboardManager.refreshScoreboard(it) }
                }
                delay(1.seconds)
            }
        }

        event<PlayerDataLeaveEvent>(forceBlocking = true) {
            player.player?.setDisplayProfile(null, false)
        }
        event<PlayerDataReloadEvent> {
            player.apply {
                if (data.hypixelData?.isGlowing == true && displayOverrides == null) {
                    player?.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, Int.MAX_VALUE, 1, true, false, false))
                } else {
                    player?.removePotionEffect(PotionEffectType.GLOWING)
                }
            }
        }
        event<PlayerDataReloadEvent>(forceBlocking = true) {
            val bukkitPlayer = player.player ?: return@event
            val profile = player.displayOverrides?.displayProfile
            val canDisguiseFreely = player.hasAtLeastRank(HypixelPackageRank.YOUTUBER, true)
            val isInGame = false //TODO: Is in game
            val shouldApplyProfile = profile == null || canDisguiseFreely || isInGame

            val playerName = player.data.overrides.nameOverride ?: player.data.hypixelData?.displayName

            if (shouldApplyProfile) bukkitPlayer.setDisplayProfile(profile)

            if (playerName != null && profile == null) {
                bukkitPlayer.playerProfile = bukkitPlayer.playerProfile.apply { name = playerName }
            }

            player.displayOverrides?.isProfileOverridden = shouldApplyProfile
        }
    }
}