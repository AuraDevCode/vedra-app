package br.com.auradevcode.vedra

import android.app.Application
import android.os.SystemClock
import br.com.auradevcode.vedra.core.LockEngine
import br.com.auradevcode.vedra.data.AppCatalog
import br.com.auradevcode.vedra.data.CredentialStore
import br.com.auradevcode.vedra.data.SettingsRepository
import br.com.auradevcode.vedra.data.VedraSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/** DI manual (ADR-005). */
class AppContainer(app: Application) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val settings = SettingsRepository(app)
    val credentials = CredentialStore(app)
    val catalog = AppCatalog(app)
    val lockEngine = LockEngine(SystemClock::elapsedRealtime)
    val unlocker = Unlocker(credentials, settings, scope)

    fun start() {
        // Falha segura: a lista de protegidos precisa estar na memória antes do primeiro evento do serviço.
        val initial = runBlocking { settings.current() }
        apply(initial)
        unlocker.restore(initial.attempts)
        scope.launch { settings.settings.collect(::apply) }
    }

    private fun apply(s: VedraSettings) {
        lockEngine.protectedPackages = s.protectedPackages.toHashSet()
        lockEngine.policy = s.relock
    }
}
