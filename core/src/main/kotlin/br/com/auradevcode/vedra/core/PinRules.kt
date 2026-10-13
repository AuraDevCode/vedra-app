package br.com.auradevcode.vedra.core

object PinRules {
    const val MIN_LENGTH = 4
    const val MAX_LENGTH = 8
    const val DEFAULT_LENGTH = 6

    fun isValid(pin: String): Boolean = pin.length in MIN_LENGTH..MAX_LENGTH && pin.all { it in '0'..'9' }

    /** PIN fácil de adivinhar: repetição, sequência ou da lista dos mais usados. */
    fun isWeak(pin: String): Boolean {
        if (pin.toSet().size == 1) return true
        val steps = pin.zipWithNext { a, b -> b - a }.toSet()
        if (steps == setOf(1) || steps == setOf(-1)) return true
        return pin in COMMON
    }

    private val COMMON = setOf(
        "1212", "6969", "1122", "2580", "0852", "112233", "121212", "131313", "159753", "147258", "102030",
    )
}
