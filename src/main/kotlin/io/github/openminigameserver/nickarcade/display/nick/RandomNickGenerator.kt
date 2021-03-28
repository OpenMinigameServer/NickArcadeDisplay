package io.github.openminigameserver.nickarcade.display.nick

import com.fasterxml.jackson.databind.json.JsonMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object RandomNickGenerator {
    private val namesQueue = ArrayDeque<String>()

    suspend fun getNewName(): String {
        fetchNamesIfNeeded()
        return namesQueue.removeFirst().let {
            var underscoreCount = 0
            it.takeWhile { c ->
                if (c == '_') {
                    underscoreCount++
                }
                underscoreCount <= 1
            }
        }
    }

    private val possibleTypes = arrayOf("boy_names", "girl_names")
    private val mapper = JsonMapper()
    private suspend fun fetchNamesIfNeeded() {
        if (namesQueue.size <= 5) {
            val result = withContext(Dispatchers.IO) {
                URL("http://names.drycodes.com/10?nameOptions=${possibleTypes.random()}").readText()
            }
            val resultNames =
                mapper.readValue<Array<String>>(result, mapper.typeFactory.constructArrayType(String::class.java))
            namesQueue.addAll(resultNames)
        }
    }

}