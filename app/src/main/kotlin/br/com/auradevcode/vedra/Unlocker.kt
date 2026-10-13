package br.com.auradevcode.vedra

import android.os.SystemClock
import br.com.auradevcode.vedra.core.AttemptLimiter
import br.com.auradevcode.vedra.core.AttemptState
import br.com.auradevcode.vedra.data.CredentialStore
import br.com.auradevcode.vedra.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed interface PinResult {
    data object Success : PinResult
    data class Wrong(val lockoutMs: Long) : PinResult
    data class LockedOut(val remainingMs: Long) : PinResult
}

/** Verificação do PIN com atraso progressivo persistido (RF-08). */
class Unlocker(
    private val credentials: CredentialStore,
    private val settings: SettingsRepository,
    private val scope: CoroutineScope,
) {
    private var limiter = AttemptLimiter(SystemClock::elapsedRealtime)

    fun restore(state: AttemptState) {
        limiter = AttemptLimiter(SystemClock::elapsedRealtime, state)
    }

    fun remainingLockoutMs(): Long = limiter.remainingMs()

    suspend fun tryPin(pin: String): PinResult {
        if (limiter.isLockedOut()) return PinResult.LockedOut(limiter.remainingMs())
        return if (credentials.verify(pin)) {
            onSuccess()
            PinResult.Success
        } else {
            val wait = limiter.onFailure()
            persist()
            PinResult.Wrong(wait)
        }
    }

    fun onSuccess() {
        limiter.onSuccess()
        persist()
    }

    private fun persist() {
        val snapshot = limiter.snapshot()
        scope.launch { settings.saveAttempts(snapshot) }
    }
}
