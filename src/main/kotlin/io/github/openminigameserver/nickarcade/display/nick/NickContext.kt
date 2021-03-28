package io.github.openminigameserver.nickarcade.display.nick

import com.destroystokyo.paper.profile.PlayerProfile
import io.github.openminigameserver.hypixelapi.models.HypixelPackageRank

data class NickContext(
    var rank: HypixelPackageRank? = null,
    var skin: PlayerProfile? = null,
    var skinName: String? = null,
    var playerName: String? = null
) {
    fun isValid(): Boolean {
        return rank != null && skin != null && skinName != null && playerName != null
    }
}