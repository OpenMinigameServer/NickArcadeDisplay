package io.github.openminigameserver.nickarcade.display.providers

import io.github.openminigameserver.nickarcade.core.data.providers.PlayerDataProvider
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.core.data.sender.player.extra.PlayerOverrides
import io.github.openminigameserver.nickarcade.display.displayOverrides

object DisplayOverridesPlayerDataProvider : PlayerDataProvider {
    override fun provideDisplayName(player: ArcadePlayer): String? {
        return player.displayOverrides?.displayProfile?.name
    }

    override fun provideOverrides(player: ArcadePlayer): PlayerOverrides? {
        return player.displayOverrides?.overrides
    }
}