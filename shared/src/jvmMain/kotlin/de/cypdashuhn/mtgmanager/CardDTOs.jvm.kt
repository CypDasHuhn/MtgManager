package de.cypdashuhn.mtgmanager

import kotlinx.serialization.json.Json

actual class CardReader {
    actual companion object {
        actual fun getCards(): List<Card> {
            val resourceStream = javaClass.getResourceAsStream("/mtgcards.json")
                ?: throw IllegalArgumentException("Resource not found: mtgcards.json")

            val jsonContent = resourceStream.bufferedReader().use { it.readText() }

            return Json.decodeFromString(jsonContent)
        }
    }
}
