package br.com.auradevcode.vedra.guard

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import br.com.auradevcode.vedra.core.Decision

/**
 * Detector de primeiro plano (ADR-002). Lê só o nome do pacote e da classe do evento (RNF-19).
 *
 * O caminho de [onAccessibilityEvent] não faz I/O nem corrotina (RNF-01) e nunca deixa
 * uma exceção derrubar o serviço (RNF-13).
 */
class VedraAccessibilityService : AccessibilityService() {

    private lateinit var host: GuardHost
    private lateinit var curtain: CurtainController
    private var transientPackages: Set<String> = BASE_TRANSIENT

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) onScreenOff()
        }
    }

    override fun onServiceConnected() {
        host = application as GuardHost
        curtain = CurtainController(this, host.secureWindows).also { it.attach() }
        GuardStatus.hideCurtain = { curtain.hide() }
        transientPackages = BASE_TRANSIENT + inputMethodPackages()
        host.lockEngine.launcherPackages = launcherPackages()
        registerReceiver(screenReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
        GuardStatus.setRunning(true)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        val startNanos = SystemClock.elapsedRealtimeNanos()
        try {
            if (pkg == packageName) {
                // Janelas do próprio Vedra (tela de bloqueio, diálogos, o app) não mudam o app em primeiro plano.
                // Não dá para confiar no className: em alguns aparelhos ele chega como "FrameLayout".
                return
            }
            val decision = host.lockEngine.onForeground(pkg, pkg in transientPackages)
            Log.d(TAG, "event $pkg/${event.className} -> $decision lockVisible=${GuardStatus.lockVisibleFor}")
            when (decision) {
                Decision.Ignore -> Unit
                Decision.Allow -> {
                    curtain.hide()
                    GuardStatus.requestDismiss()
                }
                is Decision.Lock -> {
                    // O desafio deste app já está na tela: outra janela do mesmo app não reabre nada.
                    if (GuardStatus.lockVisibleFor == decision.packageName) return
                    curtain.show()
                    val micros = (SystemClock.elapsedRealtimeNanos() - startNanos) / 1_000
                    host.launchLock(this, decision.packageName)
                    GuardStatus.recordLock(decision.packageName, micros)
                    Log.i(TAG, "lock ${decision.packageName} curtain=${micros}us")
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "event failed for $pkg", t)
        }
    }

    private fun onScreenOff() {
        try {
            val onScreen = host.lockEngine.onScreenOff() ?: return
            // Ao acender a tela, o app protegido já estará coberto.
            curtain.show()
            host.launchLock(this, onScreen)
        } catch (t: Throwable) {
            Log.e(TAG, "screen off failed", t)
        }
    }

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        GuardStatus.setRunning(false)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        GuardStatus.setRunning(false)
        GuardStatus.hideCurtain = null
        runCatching { unregisterReceiver(screenReceiver) }
        if (::curtain.isInitialized) runCatching { curtain.detach() }
        super.onDestroy()
    }

    /** Launchers instalados: a tela de recentes costuma ser a mesma janela deles (ADR-015). */
    private fun launcherPackages(): Set<String> = runCatching {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        packageManager.queryIntentActivities(home, 0).map { it.activityInfo.packageName }.toSet()
    }.getOrDefault(emptySet())

    private fun inputMethodPackages(): Set<String> = runCatching {
        getSystemService(InputMethodManager::class.java).enabledInputMethodList.map { it.packageName }.toSet()
    }.getOrDefault(emptySet())

    private companion object {
        const val TAG = "VedraGuard"

        /** Janelas que não contam como sair do app protegido (specs/04, regra de saída). */
        val BASE_TRANSIENT = setOf(
            "com.android.systemui",
            "android",
            "com.samsung.android.app.cocktailbarservice",
            "com.samsung.android.biometrics.app.setting",
            "com.google.android.permissioncontroller",
            "com.android.permissioncontroller",
            "com.samsung.android.honeyboard",
        )
    }
}
