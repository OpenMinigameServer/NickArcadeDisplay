package io.github.openminigameserver.nickarcade.display.profiles

import com.destroystokyo.paper.profile.PlayerProfile
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.core.events.data.PlayerDataJoinEvent
import io.github.openminigameserver.nickarcade.core.events.data.PlayerDataLeaveEvent
import io.github.openminigameserver.nickarcade.core.events.data.PlayerDataReloadEvent
import io.github.openminigameserver.nickarcade.core.manager.getArcadeSender
import io.github.openminigameserver.nickarcade.display.displayOverrides
import org.bukkit.entity.Player
import java.util.*


suspend fun Player.getDisplayProfile(): DumpedProfile? {
    return (getArcadeSender() as? ArcadePlayer)?.displayOverrides?.displayProfile
}

suspend inline fun reloadProfile(
    data: ArcadePlayer,
    reloadProfile: Boolean = false,
    rejoinProfile: Boolean = false,
    code: suspend ArcadePlayer.() -> Unit
) {
    if (rejoinProfile && reloadProfile) PlayerDataLeaveEvent(data, true).callEvent()
    code(data)
    if (rejoinProfile && reloadProfile) PlayerDataJoinEvent(data).callEvent()
    if (reloadProfile) PlayerDataReloadEvent(data).callEvent()
}

val oldProfilesMap = mutableMapOf<UUID, PlayerProfile>()

suspend fun Player.setDisplayProfile(profile: DumpedProfile?, reloadProfile: Boolean = false) {
    reloadProfile(getArcadeSender(), reloadProfile) {
        if (!oldProfilesMap.containsKey(uniqueId))
            oldProfilesMap[uniqueId] = playerProfile
        playerProfile = profile?.asPlayerProfile(uniqueId) ?: oldProfilesMap[uniqueId] ?: playerProfile
        displayOverrides?.displayProfile = profile
    }
}
