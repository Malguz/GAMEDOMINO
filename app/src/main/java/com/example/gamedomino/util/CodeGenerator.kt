package com.example.gamedomino.util

import kotlin.random.Random


object CodeGenerator {

    private const val ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    /**
     * Genera un código aleatorio de 6 caracteres alfanuméricos en mayúsculas.
     */
    fun generateRoomCode(length: Int = 6): String {
        return (1..length)
            .map { Random.nextInt(0, ALLOWED_CHARS.length) }
            .map { ALLOWED_CHARS[it] }
            .joinToString("")
    }
}