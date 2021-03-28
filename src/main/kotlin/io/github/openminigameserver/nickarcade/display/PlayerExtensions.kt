package io.github.openminigameserver.nickarcade.display

import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.display.nick.DisplayOverrides
import io.github.openminigameserver.nickarcade.plugin.extensions.getExtraDataValue
import io.github.openminigameserver.nickarcade.plugin.extensions.setExtraDataValue

var ArcadePlayer.displayOverrides: DisplayOverrides?
    get() = getExtraDataValue(ArcadePlayer::displayOverrides)
    set(value) {
        setExtraDataValue(ArcadePlayer::displayOverrides, value)
    }