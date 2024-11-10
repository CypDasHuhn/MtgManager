package de.cypdashuhn.mtgmanager

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform