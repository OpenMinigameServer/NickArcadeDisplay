package io.github.openminigameserver.nickarcade.display.commands

import cloud.commandframework.arguments.parser.ArgumentParseResult
import cloud.commandframework.arguments.parser.ArgumentParser
import cloud.commandframework.arguments.standard.EnumArgument
import cloud.commandframework.context.CommandContext
import cloud.commandframework.exceptions.parsing.NoInputProvidedException
import io.github.openminigameserver.hypixelapi.models.HypixelPackageRank
import io.github.openminigameserver.nickarcade.core.data.sender.ArcadeSender
import io.github.openminigameserver.nickarcade.core.data.sender.player.ArcadePlayer
import java.util.*

class NickRankParser : ArgumentParser<ArcadeSender, HypixelPackageRank> {
    override fun parse(
        commandContext: CommandContext<ArcadeSender>,
        inputQueue: Queue<String>
    ): ArgumentParseResult<HypixelPackageRank> {
        val sender = commandContext.sender as? ArcadePlayer
            ?: return ArgumentParseResult.failure(Throwable("Invalid sender"))
        val validRanks = computeValidRanks(sender.effectiveRank)

        val input = inputQueue.peek()
            ?: return ArgumentParseResult.failure(
                NoInputProvidedException(
                    EnumArgument.EnumParser::class.java,
                    commandContext
                )
            )

        for (value in validRanks) {
            if (value.name.equals(input, ignoreCase = true)) {
                inputQueue.remove()
                return ArgumentParseResult.success(value)
            }
        }

        return ArgumentParseResult.failure(
            EnumArgument.EnumParseException(
                input,
                HypixelPackageRank::class.java,
                commandContext
            )
        )
    }

    override fun suggestions(commandContext: CommandContext<ArcadeSender>, input: String): List<String> {
        val sender = commandContext.sender as? ArcadePlayer ?: return emptyList()

        val validRanks = computeValidRanks(sender.effectiveRank)
        return validRanks.map { it.name.lowercase(Locale.getDefault()) }
    }

    override fun isContextFree(): Boolean = false

    private fun computeValidRanks(effectiveRank: HypixelPackageRank): EnumSet<HypixelPackageRank> {
        return EnumSet.range(HypixelPackageRank.NONE, effectiveRank).apply {
            remove(HypixelPackageRank.NORMAL)
            remove(effectiveRank)
        }
    }

}