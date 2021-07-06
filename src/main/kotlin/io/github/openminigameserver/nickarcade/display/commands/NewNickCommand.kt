package io.github.openminigameserver.nickarcade.display.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.Hidden
import cloud.commandframework.annotations.ProxiedBy
import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import io.github.openminigameserver.hypixelapi.models.HypixelPackageRank
import io.github.openminigameserver.nickarcade.chat.utils.ChatInput
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import io.github.openminigameserver.nickarcade.core.data.sender.player.extra.PlayerOverrides
import io.github.openminigameserver.nickarcade.core.data.sender.player.extra.RuntimeExtraDataTag
import io.github.openminigameserver.nickarcade.core.manager.getArcadeSender
import io.github.openminigameserver.nickarcade.display.commands.TestCommands.ranksRange
import io.github.openminigameserver.nickarcade.display.displayOverrides
import io.github.openminigameserver.nickarcade.display.managers.ProfilesManager
import io.github.openminigameserver.nickarcade.display.nick.DisplayOverrides
import io.github.openminigameserver.nickarcade.display.nick.NickContext
import io.github.openminigameserver.nickarcade.display.nick.RandomNickGenerator
import io.github.openminigameserver.nickarcade.display.profiles.DumpedProfile
import io.github.openminigameserver.nickarcade.display.profiles.DumpedProperty
import io.github.openminigameserver.nickarcade.display.profiles.setDisplayProfile
import io.github.openminigameserver.nickarcade.plugin.extensions.clickEvent
import io.github.openminigameserver.nickarcade.plugin.extensions.command
import io.github.openminigameserver.nickarcade.plugin.extensions.launch
import io.github.openminigameserver.nickarcade.plugin.helper.commands.RequiredRank
import io.github.openminigameserver.profile.models.Profile
import kotlinx.coroutines.delay
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.seconds

object NewNickCommand {
    private val nickContextTag = RuntimeExtraDataTag.of<NickContext>("nickcontext")

    private var ArcadePlayer.nickContext: NickContext
        get() = this[nickContextTag] ?: NickContext().also {
            this.nickContext = it
        }
        set(value) {
            this[nickContextTag] = value
        }

    @RequiredRank(HypixelPackageRank.SUPERSTAR)
    @CommandMethod("nick rank <rank>")
    fun nickSetRank(
        sender: ArcadePlayer,
        @Argument("rank", parserName = "nickrank") rank: HypixelPackageRank
    ) {
        sender.nickContext.rank = rank
        sender.audience.sendMessage(text {
            it.append(text("Set your nick rank to ", GREEN))
            if (rank <= HypixelPackageRank.NORMAL) {
                it.append(text("DEFAULT", GRAY))
            } else {
                it.append(text(rank.defaultPrefix.trim().replace("[", "").replace("]", "")))
            }
            it.append(text("!", GREEN))
        })
    }

    @CommandMethod("nick help")
    @ProxiedBy("nick")
    fun nickHelp(sender: ArcadePlayer) {
        //First page
        sender.openBook(text {
            it.append(text("Nicknames allow you to play with a different username to not get recognized."))
                .append(newline())
            it.append(newline())
            it.append(text("All rules still apply.")).append(newline())
            it.append(text("You can still be reported and all name history is stored."))
                .append(newline())
            it.append(newline())
            it.append(
                text(
                    "➤ I understand, set up my nickname",
                    Style.style(TextDecoration.UNDERLINED)
                )
                    .hoverEvent(
                        text("Click here to proceed")
                    )
                    .clickEvent(ClickEvent.runCommand("/nick help start"))
            )
        })
    }

    @CommandMethod("nick start")
    fun nickStart(sender: ArcadePlayer) {
        nickStartHelp(sender)
    }

    @CommandMethod("nick help start")
    fun nickStartHelp(sender: ArcadePlayer) {
        //Second page
        val isYoutuber = sender.hasAtLeastRank(HypixelPackageRank.YOUTUBER, true)

        val page = text { page ->
            page.append(text("Let's get you set up with your nickname!"))
                .append(newline())
            page.append(text {
                it.append(text("First, you'll need to choose which "))
                it.append(text("RANK", Style.style(TextDecoration.BOLD)))
                it.append(text(" you would like to be shown as when nicked.").append(newline()))
            })
                .append(newline())

            val ranks = mutableListOf(
                HypixelPackageRank.NONE,
                HypixelPackageRank.VIP,
                HypixelPackageRank.VIP_PLUS,
                HypixelPackageRank.MVP,
                HypixelPackageRank.MVP_PLUS,
            )
            if (isYoutuber)
                ranks.add(HypixelPackageRank.SUPERSTAR)

            ranks.forEach { rank ->
                page.append(text { rankText ->
                    val color = ChatColor.getLastColors(rank.defaultPrefix)
                    var name = (rank.defaultPrefix).dropWhile { it != '[' }.trim().removeSurrounding("[", "]")
                    if (rank == HypixelPackageRank.NONE) {
                        name = "DEFAULT"
                    }

                    val fullName = color + name
                    rankText
                        .append(text("➤ "))
                        .append(text(fullName))
                        .append(newline()).hoverEvent(text("Click here to be shown as $fullName"))
                }.clickEvent(ClickEvent.runCommand("/nick help rank $rank")))
            }
        }

        sender.openBook(page)
    }


    private suspend fun getRandomProfileName() = (RandomNickGenerator.getNewName()).take(16)

    @Hidden
    @RequiredRank(HypixelPackageRank.SUPERSTAR)
    @CommandMethod("nick help rank <rank>")
    fun nickHelpSetRank(
        sender: ArcadePlayer,
        @Argument("rank", parserName = "nickrank") rank: HypixelPackageRank
    ) {
        nickSetRank(sender, rank)
        //Page three
        val page = text { page ->
            page.append(text("Awesome! Now, which "))
                .append(text("SKIN", Style.style(TextDecoration.BOLD)))
                .append(text(" would you like to have while nicked?")).append(newline())
                .append(newline())

            page.append(
                text("➤ My normal skin").hoverEvent(
                    text("Click here to use your normal skin")
                        .append(newline())
                        .append(text("WARNING: ", RED))
                        .append(text("Players will be able to know who you are if you use this option."))

                ).clickEvent(setSkinClickEvent("self")).append(newline())
            )

            page.append(
                text("➤ Steve/Alex skin").hoverEvent(
                    text("Click here to use a Steve/Alex skin")
                ).clickEvent(setSkinClickEvent(if (Random.nextBoolean()) "steve" else "alex"))
                    .append(newline())
            )

            val profile = sender.nickContext.skinName
            if (profile != null) {
                page.append(
                    text("➤ Reuse $profile").hoverEvent(
                        text("Click here to reuse your previous skin")
                    ).clickEvent(setSkinClickEvent(profile)).append(newline())
                )
            }

            page.append(
                text("➤ Random skin").hoverEvent(
                    text("Click here to use a random preset skin")
                ).clickEvent(setSkinClickEvent(ProfilesManager.profiles.random().name)).append(newline())
            )
        }
        sender.openBook(page)
    }


    @RequiredRank(HypixelPackageRank.SUPERSTAR)
    @CommandMethod("nick skin <skin>")
    fun nickSetSkin(
        sender: ArcadePlayer,
        @Argument("skin") skin: String
    ) {
        sender.nickContext.skin = when (skin) {
            "alex" -> alexProfile
            else -> (ProfilesManager.profiles.firstOrNull { it.name == skin }?.asPlayerProfile(sender.uuid)
                ?: steveProfile)
        }
        sender.nickContext.skinName = skin
        sender.audience.sendMessage(text {
            it.append(text("Your skin has been set to ", GREEN))
            it.append(text(skin, GREEN))
            it.append(text(".", GREEN))
        })
    }

    private val validNamePattern = Regex("^[a-zA-Z0-9_]{3,16}\$")

    @Hidden
    @RequiredRank(HypixelPackageRank.SUPERSTAR)
    @CommandMethod("nick help skin <skin>")
    fun nickHelpSetSkin(
        sender: ArcadePlayer,
        @Argument("skin") skin: String
    ) = command(sender) {
        nickSetSkin(sender, skin)
        val isYoutuber = sender.hasAtLeastRank(HypixelPackageRank.YOUTUBER, true)
        val page = text { page ->
            page.append(text("Alright, now you'll need to choose the "))
                .append(text("NAME", Style.style(TextDecoration.BOLD)))
                .append(text(" to use!")).append(newline())
                .append(newline())

            if (isYoutuber) {
                page.append(
                    text("➤ Enter a name").hoverEvent(
                        text("Click to enter the name to use")
                    ).clickEvent {
                        sendMessage(
                            text(
                                "Please type the name you want to use in chat.",
                                GOLD
                            )
                        )
                        ChatInput.requestInput(this, onSuccess = {
                            launch {
                                this@requestInput.performCommand(
                                    setNickNameAndRespawn(it).value().removePrefix("/")
                                )
                            }
                        }, isValid = {
                            return@requestInput validNamePattern.matchEntire(it) != null
                        })
                    }.append(newline())
                )
            }
            page.append(
                text("➤ Use a random name").hoverEvent(
                    text("Click to use a randomly generated name")
                ).clickEvent(setRandomNickNameAndRespawn()).append(newline())
            )

            val name = sender.displayOverrides?.displayProfile?.displayName
            if (name != null) {
                page.append(
                    text("➤ Reuse '$name'").hoverEvent(
                        text("Click here to reuse the previous nickname.")
                    ).clickEvent(setNickNameAndRespawn(name)).append(newline())
                ).append(newline())
            }
        }

        sender.openBook(page)
    }

    private fun setRandomNickNameAndRespawn() = setNickNameAndRespawn("random-gui")


    @Hidden
    @RequiredRank(HypixelPackageRank.SUPERSTAR)
    @CommandMethod("nick actualsetname <name>")
    fun nickSetName(
        sender: ArcadePlayer,
        @Argument("name") name: String
    ) = command(sender) {
        var finalName = name
        if (finalName == "random") {
            finalName = getRandomProfileName()
        }
        sender.nickContext.playerName = finalName
        sender.nickContext.skin?.name = finalName
        sender.player?.applyNickContext(sender.nickContext)
    }

    @RequiredRank(HypixelPackageRank.SUPERSTAR)
    @CommandMethod("nick random")
    fun nickRandom(
        sender: ArcadePlayer,
    ) = command(sender) {
        nickSetRank(sender, ranksRange.random())
        nickSetSkin(sender, ProfilesManager.profiles.random().name)
        nickSetName(sender, getRandomProfileName())
        sender.player?.applyNickContext(sender.nickContext)
    }

    @RequiredRank(HypixelPackageRank.SUPERSTAR)
    @CommandMethod("nick reset")
    @ProxiedBy("unnick")
    fun nickReset(
        sender: ArcadePlayer,
    ) = command(sender) {
        sender.player?.applyNickContext(null)
    }

    @Hidden
    @RequiredRank(HypixelPackageRank.SUPERSTAR)
    @CommandMethod("nick help actualsetname <name>")
    fun nickHelpSetName(
        sender: ArcadePlayer,
        @Argument("name") name: String
    ) = command(sender) {
        if (name == "random-gui") {
            sender.audience.sendMessage(text("Processing request. Please wait...", YELLOW))
            val newName = RandomNickGenerator.getNewName()

            sender.openBook(
                text { page ->
                    page.append(text("We've generated a random username for you:"))
                    page.append(newline())
                    page.append(text(newName, Style.style(TextDecoration.BOLD)))
                    page.append(newline())
                    page.append(newline())

                    repeat(7) { page.append(space()) }
                    page.append(
                        text("USE NAME", DARK_GREEN, TextDecoration.BOLD, TextDecoration.UNDERLINED).clickEvent(
                            setNickNameAndRespawn(newName)
                        ).hoverEvent(text("Click here to use this name."))
                    )
                    page.append(newline())

                    repeat(6) { page.append(space()) }
                    page.append(
                        text("TRY AGAIN", RED, TextDecoration.BOLD, TextDecoration.UNDERLINED).clickEvent(
                            setRandomNickNameAndRespawn()
                        ).hoverEvent(text("Click here to generate another name."))
                    )
                    page.append(newline())

                }
            )

            return@command
        }

        val isYoutuber = sender.hasAtLeastRank(HypixelPackageRank.YOUTUBER, true)
        nickSetName(sender, name)
        delay(Duration.seconds(1))
        sender.openBook(
            text { page ->
                page.append(text("You have finished setting up your nickname!")).append(newline())
                page.append(newline())
                page.append(text("When you go into a game, you will be nicked as "))
                page.append(
                    text(
                        (sender.nickContext.rank
                            ?: HypixelPackageRank.NORMAL).defaultPrefix + sender.nickContext.playerName!!
                    )
                )
                page.append(text(".", Style.empty()))
                if (!isYoutuber) {
                    page.append(text("You will not be nicked in lobbies."))
                }
                page.append(newline())
                page.append(newline())

                page.append(
                    text("To go back to being your usual self, type:")
                        .append(newline())
                        .append(text("/unnick", Style.style(TextDecoration.BOLD)))
                )
            }
        )
    }

    private fun setNickNameAndRespawn(name: String): ClickEvent =
        ClickEvent.runCommand("/nick help actualsetname $name")

    private fun setSkinClickEvent(skin: String): ClickEvent {
        return ClickEvent.runCommand("/nick help skin $skin")
    }


    private fun createPlayerProfile(uuid: UUID, name: String, properties: List<ProfileProperty>): PlayerProfile {
        return Bukkit.createProfile(uuid, name).apply {
            setProperties(properties)
            displayId = uuid
        }
    }

    private val steveProfile by lazy {
        createPlayerProfile(
            UUID.fromString("c06f8906-4c8a-4911-9c29-ea1dbd1aab82"),
            name = "skin902537698",
            properties = mutableListOf(
                ProfileProperty(
                    "textures",
                    "ewogICJ0aW1lc3RhbXAiIDogMTYxMDMwNjk2Njg2MCwKICAicHJvZmlsZUlkIiA6ICJjMDZmODkwNjRjOGE0OTExOWMyOWVhMWRiZDFhYWI4MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfU3RldmUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWE0YWY3MTg0NTVkNGFhYjUyOGU3YTYxZjg2ZmEyNWU2YTM2OWQxNzY4ZGNiMTNmN2RmMzE5YTcxM2ViODEwYiIKICAgIH0KICB9Cn0=",
                    "s+BUn/4Jom5LHFxYeoaUGDiok6xf3r+l6uh2Sv94zqFtUlYGP7cCTzCRqnwq1qGajKKYVGTY30tZ2hwcKtcS8mxkcqS8d2NHwpaFE0MHPWnGxg/7IIPBboYnWFTulWEeoQfl4+b+Q5HPo1+YV7rkM5mQ3+5Wo+h1TQzSUK+YtG3MOHPsYFJH4v3datWBqW9dG0PxPIx75l07RRGxpNYAZFKoptT06bdntuV5N0RoW4ZL0YtOi4vb06hy6Bl+pp7eilhftupQIc0PWCbT1C+VbiihlLDw/xCriDJxK8a1TxPME0AUAAEO6DlCpSzj3ge8zvv0sSHu58JTcdiolrP2r47gnMJ9U/6AXLaBT64Lcb2HWUMGXLeQP2W9c/NiHYxhgU7G1U/GZcwUFHf5iqtCsqQzTWiRehkMIoyzIGIs/IDxfFTESxhTx8uTqp3Fk+e/dAU7L7hHXLDhs20BJva4NnYVL/FWdIxdeg1Uta+loG96nPfaARvY/dZEO88gNc3JCvAqwv3RJJsWDXvr6rj2IoPtloo3Clpw0k6dx04rV0zGyTgE+fhSuQSH1IqFyhCfZYItrmP9Q8HhoI0ZYlCD3mxY2a91OPOVSjj3Lv8ewl2h3bXcDtDnfPuDS9xaoVEyABrOugA78UVwi6QgmSr+Vm9XTcv4wNFs1AAZVkcnxP0="
                )
            )
        )
    }

    private val alexProfile by lazy {
        createPlayerProfile(
            UUID.fromString("c06f8906-4c8a-4911-9c29-ea1dbd1aab82"),
            name = "skin396213",
            properties = mutableListOf(
                ProfileProperty(
                    "textures",
                    "eyJ0aW1lc3RhbXAiOjE1NDAwODc3MDUyNzYsInByb2ZpbGVJZCI6IjZhYjQzMTc4ODlmZDQ5MDU5N2Y2MGY2N2Q5ZDc2ZmQ5IiwicHJvZmlsZU5hbWUiOiJNSEZfQWxleCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODNjZWU1Y2E2YWZjZGIxNzEyODVhYTAwZTgwNDljMjk3YjJkYmViYTBlZmI4ZmY5NzBhNTY3N2ExYjY0NDAzMiIsIm1ldGFkYXRhIjp7Im1vZGVsIjoic2xpbSJ9fX19",
                    "k62gX82u1JBsUMXPAzADWTlpzrBNti01wwnJ+l9TnNP7PuZ7dvlHztAsIr3H4EdBWmyjq3lpdWRgRkuQ0BiFai/cNuqTJBRJENIXH0pdNQnLBkyJmGsFAYHrufEtUr0ikSF2afjAY65wH6v3iCiMofnEXuzB/xrhjIlGl83LCeTLSWJ36d321XCiP2hJ/LF9a6UY7x4Po2qSzFyC5naqm84+BaTzRlnhX5spJjkexJ5N9APhcmzzALybwEfCXmeRMct+8s8qTdmHPm9H8hNg+FxsOekSPgcbZTaLmz8j1tfiX0SBPPhjPNBA604pP53a9ZcOt+6eMJXWy79i9BIlScua4iC5BT7WXitck5h+kNT0mBmMt+YEcbw0VVAVQzE29+MPC+QmrLky1vqZoXb/wsZbnZmbD6npR3b+Fnd0wNW5u83P0ssRUDzqDhPAryPayzDLQ+jLP1GSMA12etFcOE7cFkyv2H1Tz/U9iLJ0kug19dCq6GBB4EkXS51VKbCD23e2gbHwogsW0gzAKiu9d4IcA9lRUB9QMGbHQNzJnD+nU9uxGFryrXx2q0VOcaszmbuC0IKyWSxLtDenO5CBng5Wwlwtmcg9fkqawMhUAuT8+7vyBzV/Oj0gtVCxLEm/cxkXBW6O833p4CmQfXd0Ry2y73GMt2FPHFQgAFS7Q/w="
                )
            )
        )
    }

    private fun ArcadePlayer.openBook(page: Component) {
        audience.openBook(Book.book(text("NickArcade"), text("NickAc"), page))
    }

    private suspend fun Player.applyNickContext(context: NickContext?) {
        val player = getArcadeSender()
        val bukkitPlayer = player.player ?: return
        if (context == null) {
            player.displayOverrides?.resetDisguise()
            bukkitPlayer.setDisplayProfile(null, true)
            player.audience.sendMessage(text("Your nick has been reset!", GREEN))
            return
        }

        if (!player.nickContext.isValid()) {
            player.audience.sendMessage(
                text(
                    "You have not finished setting up your nickname! Run /nick to fix this issue.",
                    RED
                )
            )
            return
        }

        val profile = context.skin!!.toDumpedProfile()

        player.displayOverrides = DisplayOverrides().apply {
            isPartyDisguise = false
            displayProfile = profile.copy(displayName = context.skinName!!)

            overrides =
                PlayerOverrides(
                    context.rank,
                    networkLevel = Random.nextInt(1, 50).toLong(),
                    isLegacyPlayer = false
                )
        }

        bukkitPlayer.setDisplayProfile(profile, true)
        player.audience.sendMessage(text("You are now nicked as ${profile.name}!", GREEN))
    }


    fun Profile.toDumpedProfile(): DumpedProfile {
        val properties = mutableMapOf<String, List<DumpedProperty>>()
        val profileTextures = textures?.raw
        if (profileTextures != null) {
            properties["textures"] =
                listOf(DumpedProperty("textures", profileTextures.value, profileTextures.signature))
        }
        return DumpedProfile(name!!, name!!, name!!, uuid!!, properties)
    }

    private fun PlayerProfile.toDumpedProfile(): DumpedProfile {
        val properties = mutableMapOf<String, List<DumpedProperty>>()
        if (hasTextures()) {
            val value = this.properties.first()
            properties[value.name] = listOf(DumpedProperty(value.name, value.value, value.signature ?: ""))
        }
        return DumpedProfile(name!!, name!!, name!!, this.displayId ?: this.id!!, properties)
    }
}