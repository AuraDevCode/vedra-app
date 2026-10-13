package br.com.auradevcode.vedra.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.auradevcode.vedra.core.AttemptState
import br.com.auradevcode.vedra.core.RelockMode
import br.com.auradevcode.vedra.core.RelockPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class VedraSettings(
    val protectedPackages: Set<String> = emptySet(),
    val relock: RelockPolicy = RelockPolicy.Default,
    val biometricEnabled: Boolean = false,
    val onboardingDone: Boolean = false,
    val attempts: AttemptState = AttemptState(),
)

private val Context.settingsStore by preferencesDataStore(name = "vedra_settings")

class SettingsRepository(context: Context) {
    private val store = context.applicationContext.settingsStore

    val settings: Flow<VedraSettings> = store.data.map { it.toSettings() }

    suspend fun current(): VedraSettings = settings.first()

    suspend fun setProtected(packageName: String, protected: Boolean) {
        store.edit { prefs ->
            val now = prefs[PROTECTED].orEmpty()
            prefs[PROTECTED] = if (protected) now + packageName else now - packageName
        }
    }

    suspend fun setProtectedPackages(packages: Set<String>) {
        store.edit { it[PROTECTED] = packages }
    }

    suspend fun setRelock(policy: RelockPolicy) {
        store.edit {
            it[RELOCK_MODE] = policy.mode.name
            it[RELOCK_TIMEOUT] = policy.timeoutMs
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        store.edit { it[BIOMETRIC] = enabled }
    }

    suspend fun setOnboardingDone() {
        store.edit { it[ONBOARDING] = true }
    }

    suspend fun saveAttempts(state: AttemptState) {
        store.edit {
            it[FAILURES] = state.consecutiveFailures
            it[LOCKOUT_REMAINING] = state.lockoutRemainingMs
        }
    }

    private fun Preferences.toSettings() = VedraSettings(
        protectedPackages = this[PROTECTED].orEmpty(),
        relock = RelockPolicy(
            mode = this[RELOCK_MODE]?.let { runCatching { RelockMode.valueOf(it) }.getOrNull() } ?: RelockMode.IMMEDIATE,
            timeoutMs = this[RELOCK_TIMEOUT] ?: 0,
        ),
        biometricEnabled = this[BIOMETRIC] ?: false,
        onboardingDone = this[ONBOARDING] ?: false,
        attempts = AttemptState(this[FAILURES] ?: 0, this[LOCKOUT_REMAINING] ?: 0),
    )

    private companion object {
        val PROTECTED = stringSetPreferencesKey("protected_packages")
        val RELOCK_MODE = stringPreferencesKey("relock_mode")
        val RELOCK_TIMEOUT = longPreferencesKey("relock_timeout_ms")
        val BIOMETRIC = booleanPreferencesKey("biometric_enabled")
        val ONBOARDING = booleanPreferencesKey("onboarding_done")
        val FAILURES = intPreferencesKey("attempt_failures")
        val LOCKOUT_REMAINING = longPreferencesKey("attempt_lockout_remaining_ms")
    }
}
