package br.com.auradevcode.vedra.core

/**
 * Decide, a cada troca de janela, se o app em primeiro plano deve ser bloqueado.
 *
 * Fica no caminho crítico do serviço de acessibilidade (RNF-01): só consultas em memória.
 * As sessões nunca vão para o disco (ADR-009): se o processo morre, tudo volta a bloquear.
 *
 * @param clock relógio monotônico em ms (ex.: `SystemClock.elapsedRealtime`).
 */
class LockEngine(private val clock: () -> Long) {

    @Volatile
    var protectedPackages: Set<String> = emptySet()

    @Volatile
    var policy: RelockPolicy = RelockPolicy.Default

    /** Pacote -> momento em que o usuário saiu dele, ou [IN_FOREGROUND] enquanto está nele. */
    private val sessions = HashMap<String, Long>()

    @Volatile
    var foreground: String? = null
        private set

    @Synchronized
    fun onForeground(packageName: String, transient: Boolean = false): Decision {
        if (transient) return Decision.Ignore

        val previous = foreground
        if (previous != null && previous != packageName && sessions[previous] == IN_FOREGROUND) {
            if (policy.mode == RelockMode.IMMEDIATE) sessions.remove(previous) else sessions[previous] = clock()
        }
        foreground = packageName

        if (packageName !in protectedPackages) return Decision.Allow

        val leftAt = sessions[packageName]
        if (leftAt != null && (leftAt == IN_FOREGROUND || withinGrace(leftAt))) {
            sessions[packageName] = IN_FOREGROUND
            return Decision.Allow
        }
        sessions.remove(packageName)
        return Decision.Lock(packageName)
    }

    @Synchronized
    fun onUnlocked(packageName: String) {
        sessions[packageName] = if (foreground == packageName) IN_FOREGROUND else clock()
    }

    @Synchronized
    fun isUnlocked(packageName: String): Boolean {
        val leftAt = sessions[packageName] ?: return false
        return leftAt == IN_FOREGROUND || withinGrace(leftAt)
    }

    /** Tela apagou: encerra todas as sessões. Devolve o app protegido que estava na tela, se houver. */
    @Synchronized
    fun onScreenOff(): String? {
        sessions.clear()
        return foreground?.takeIf { it in protectedPackages }
    }

    @Synchronized
    fun lockAll() = sessions.clear()

    private fun withinGrace(leftAt: Long): Boolean = when (policy.mode) {
        RelockMode.IMMEDIATE -> false
        RelockMode.AFTER_TIMEOUT -> clock() - leftAt <= policy.timeoutMs
        RelockMode.SCREEN_OFF -> true
    }

    private companion object {
        const val IN_FOREGROUND = Long.MIN_VALUE
    }
}
