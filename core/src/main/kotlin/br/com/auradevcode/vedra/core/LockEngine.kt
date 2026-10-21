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

    /**
     * Launchers do aparelho. Na maioria dos Androids, a tela de recentes é a mesma janela do launcher,
     * então não dá para distinguir "abriu os recentes" de "foi para o Início" (ADR-015).
     */
    @Volatile
    var launcherPackages: Set<String> = emptySet()

    @Volatile
    var policy: RelockPolicy = RelockPolicy.Default

    /** Pacote -> momento em que o usuário saiu dele, ou [IN_FOREGROUND] enquanto está nele. */
    private val sessions = HashMap<String, Long>()

    /** App protegido que o usuário deixou indo só até o launcher (recentes ou Início), sem abrir outro app. */
    private var quickExit: String? = null

    @Volatile
    var foreground: String? = null
        private set

    @Synchronized
    fun onForeground(packageName: String, transient: Boolean = false): Decision {
        if (transient) return Decision.Ignore

        val previous = foreground
        val toLauncher = packageName in launcherPackages
        if (previous != packageName) {
            if (previous != null && sessions[previous] == IN_FOREGROUND) {
                sessions[previous] = clock()
                quickExit = if (toLauncher) previous else null
                if (!toLauncher && policy.mode == RelockMode.IMMEDIATE) sessions.remove(previous)
            } else if (!toLauncher && packageName != quickExit) {
                // Do launcher para outro app: a tolerância de volta rápida acaba aqui.
                quickExit?.let { if (policy.mode == RelockMode.IMMEDIATE) sessions.remove(it) }
                quickExit = null
            }
        }
        foreground = packageName

        if (packageName !in protectedPackages) return Decision.Allow

        val leftAt = sessions[packageName]
        val quickReturn = quickExit == packageName && leftAt != null && clock() - leftAt <= QUICK_RETURN_MS
        if (packageName == quickExit) quickExit = null
        if (leftAt != null && (leftAt == IN_FOREGROUND || quickReturn || withinGrace(leftAt))) {
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
        quickExit = null
        return foreground?.takeIf { it in protectedPackages }
    }

    @Synchronized
    fun lockAll() {
        sessions.clear()
        quickExit = null
    }

    private fun withinGrace(leftAt: Long): Boolean = when (policy.mode) {
        RelockMode.IMMEDIATE -> false
        RelockMode.AFTER_TIMEOUT -> clock() - leftAt <= policy.timeoutMs
        RelockMode.SCREEN_OFF -> true
    }

    companion object {
        private const val IN_FOREGROUND = Long.MIN_VALUE

        /** Espiar os recentes ou o Início e voltar ao mesmo app dentro deste tempo não pede o PIN. */
        const val QUICK_RETURN_MS = 5_000L
    }
}
