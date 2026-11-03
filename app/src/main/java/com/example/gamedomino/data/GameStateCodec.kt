package com.example.gamedomino.data

import com.example.gamedomino.model.GameState
import kotlinx.serialization.json.Json

/**
 * Turns a [GameState] into a JSON String and back.
 * We store that String in Firebase (/rooms/{id}/tableroState) so both devices
 * can share the exact same game. Pure Kotlin, so it can be unit tested.
 */
object GameStateCodec {

    private val json = Json {
        ignoreUnknownKeys = true   // be safe if the shape changes between app versions
        encodeDefaults = true      // always write default fields (round, status, etc.)
    }

    /** GameState -> JSON String. */
    fun encode(state: GameState): String = json.encodeToString(GameState.serializer(), state)

    /** JSON String -> GameState, or null when the text is empty or corrupt. */
    fun decode(text: String?): GameState? {
        if (text.isNullOrBlank()) return null
        return try {
            json.decodeFromString(GameState.serializer(), text)
        } catch (e: Exception) {
            null
        }
    }
}
