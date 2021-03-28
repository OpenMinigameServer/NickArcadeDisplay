package io.github.openminigameserver.nickarcade.display.profiles

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import java.util.*

data class DumpedProfile(
    val tabShownName: String = "",
    val displayName: String = "",
    val name: String,
    val uuid: UUID,
    val properties: Map<String, List<DumpedProperty>>
) {
    fun asPlayerProfile(originalId: UUID): PlayerProfile {
        val profile = Bukkit.createProfile(originalId, name).apply { displayId = uuid }
        properties.values.firstOrNull()?.map { ProfileProperty(it.name, it.value, it.signature) }?.let {
            profile.properties.addAll(
                it
            )
        }
        return profile.apply { this.name = this@DumpedProfile.name }
    }
}
