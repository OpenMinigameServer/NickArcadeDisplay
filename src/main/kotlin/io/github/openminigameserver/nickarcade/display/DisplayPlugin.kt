package io.github.openminigameserver.nickarcade.display

import io.github.openminigameserver.nickarcade.core.commandAnnotationParser
import io.github.openminigameserver.nickarcade.core.manager.PlayerDataProviderManager
import io.github.openminigameserver.nickarcade.display.commands.NewNickCommand
import io.github.openminigameserver.nickarcade.display.commands.NickCommandHelper
import io.github.openminigameserver.nickarcade.display.commands.TestCommands
import io.github.openminigameserver.nickarcade.display.events.PlayerDataEvents
import io.github.openminigameserver.nickarcade.display.managers.ProfilesManager
import io.github.openminigameserver.nickarcade.display.providers.DisplayOverridesPlayerDataProvider
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class DisplayPlugin : JavaPlugin() {

    private fun loadProfilesManager() {
        val directory = File(dataFolder, "profiles").also { it.mkdirs() }
        ProfilesManager.loadProfiles(directory)
        logger.info("Loaded ${ProfilesManager.profiles.count()} profiles from dump!")
    }

    override fun onEnable() {
        NickCommandHelper.register()
        commandAnnotationParser.parse(NewNickCommand)
        commandAnnotationParser.parse(TestCommands)
        PlayerDataProviderManager.registerProvider(DisplayOverridesPlayerDataProvider)
        loadProfilesManager()
        PlayerDataEvents.registerHandlers()
    }
}