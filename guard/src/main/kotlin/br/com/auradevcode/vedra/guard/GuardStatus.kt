package br.com.auradevcode.vedra.guard

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

data class LastLock(val packageName: String, val curtainMicros: Long, val atMillis: Long)

/** Ponte entre o serviço e as telas do app (mesmo processo). Ver P1b em specs/05. */
object GuardStatus {
    private val main = Handler(Looper.getMainLooper())

    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()

    private val _lastLock = MutableStateFlow<LastLock?>(null)
    val lastLock: StateFlow<LastLock?> = _lastLock.asStateFlow()

    private val _dismissLock = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** O usuário saiu da tela de bloqueio sem desbloquear (Início, recentes): ela deve fechar. */
    val dismissLock: SharedFlow<Unit> = _dismissLock.asSharedFlow()

    /** Pacote cujo desafio está visível agora, ou null. Escrito pela tela de bloqueio. */
    @Volatile
    var lockVisibleFor: String? = null
        private set

    @Volatile
    internal var hideCurtain: (() -> Unit)? = null

    /** A tela de bloqueio desenhou e cobre o app protegido: a cortina pode sair. */
    fun onLockScreenShown(packageName: String) {
        lockVisibleFor = packageName
        main.post { hideCurtain?.invoke() }
    }

    fun onLockScreenHidden() {
        lockVisibleFor = null
    }

    internal fun setRunning(value: Boolean) {
        _running.value = value
    }

    internal fun recordLock(packageName: String, curtainMicros: Long) {
        _lastLock.value = LastLock(packageName, curtainMicros, System.currentTimeMillis())
    }

    internal fun requestDismiss() {
        _dismissLock.tryEmit(Unit)
    }
}
