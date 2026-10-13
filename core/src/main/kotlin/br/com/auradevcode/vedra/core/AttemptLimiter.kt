package br.com.auradevcode.vedra.core

/** Estado que sobrevive ao reinício do app e do aparelho (RF-08). */
data class AttemptState(val consecutiveFailures: Int = 0, val lockoutRemainingMs: Long = 0)

/**
 * Atraso progressivo contra força bruta no PIN (RF-08).
 * Usa relógio monotônico: mudar a hora do sistema não encurta a espera (A9).
 */
class AttemptLimiter(private val clock: () -> Long, initial: AttemptState = AttemptState()) {

    var failures: Int = initial.consecutiveFailures
        private set

    private var lockedUntil: Long = clock() + initial.lockoutRemainingMs

    fun remainingMs(): Long = (lockedUntil - clock()).coerceAtLeast(0)

    fun isLockedOut(): Boolean = remainingMs() > 0

    /** Registra um erro e devolve a espera imposta (0 se não houver). */
    fun onFailure(): Long {
        failures++
        val wait = lockoutFor(failures)
        if (wait > 0) lockedUntil = clock() + wait
        return wait
    }

    fun onSuccess() {
        failures = 0
        lockedUntil = 0
    }

    fun snapshot() = AttemptState(failures, remainingMs())

    companion object {
        fun lockoutFor(failures: Int): Long = when {
            failures >= 15 -> 15 * 60_000L
            failures == 10 -> 5 * 60_000L
            failures == 5 -> 30_000L
            else -> 0
        }
    }
}
