package ir.fastlove.app.core.util

import kotlin.random.Random

/** Multiplatform id helpers (no java.util.UUID). */
object Ids {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    fun referralCode(): String = "FL" + randomToken(8)

    fun proposalId(): String = "dp_" + randomToken(10)

    fun randomToken(length: Int): String =
        buildString { repeat(length) { append(ALPHABET[Random.nextInt(ALPHABET.length)]) } }
}
