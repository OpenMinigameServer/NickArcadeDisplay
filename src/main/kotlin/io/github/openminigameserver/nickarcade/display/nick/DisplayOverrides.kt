package io.github.openminigameserver.nickarcade.display.nick

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.openminigameserver.nickarcade.core.data.sender.player.extra.PlayerOverrides
import io.github.openminigameserver.nickarcade.display.profiles.DumpedProfile

data class DisplayOverrides(
    var displayProfile: DumpedProfile? = null,
    var overrides: PlayerOverrides? = null,
    @JsonIgnore var isProfileOverridden: Boolean = false,
    var isPartyDisguise: Boolean = false
) {
    @JsonIgnore
    fun resetDisguise() {
        displayProfile = null
        overrides = null
        isPartyDisguise = false
    }
}