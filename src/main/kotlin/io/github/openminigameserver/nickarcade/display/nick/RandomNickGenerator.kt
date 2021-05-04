package io.github.openminigameserver.nickarcade.display.nick

import kotlin.random.Random
import kotlin.random.nextInt

object RandomNickGenerator {
    private val namesQueue = ArrayDeque<String>()

    suspend fun getNewName(): String {
        fetchNamesIfNeeded()
        return namesQueue.removeFirst()
    }

    private val nameFormats = arrayOf(
        "First_LastYear",
        "FirstDoesGaming",
        "smhFirst",
        "Firstxd",
        "ilyFirst",
        "FirstToggled",
        "LilLast",
        "SweetFirst",
        "DepressedFirst",
        "FirstIsHere",
        "FirstNotFound",
        "SaltyFirst",
        "_FirstYear_",
        "ImFirst",
        "yFirst",
        "FirstGames",
        "FirstYT",
        "NotFirst",
        "ProbablyFirst",
        "FirstWasFound",
        "FirstWasTaken",
        "ShutUpFirst",
    )

    private fun fetchNamesIfNeeded() {
        if (namesQueue.size <= 5) {


            var count = 0
            while (count < 10) {
                val name = nameFormats.random().replace("First", firstNames.random())
                    .replace("Last", lastNames.random()).replace("Year", Random.nextInt(1990..2021).toString())

                //Make sure to generate names with at most 16 chars
                if (name.length <= 16) {
                    namesQueue += name
                    count++
                }
            }
        }
    }

}