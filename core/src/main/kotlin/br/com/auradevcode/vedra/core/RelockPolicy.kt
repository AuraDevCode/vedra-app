package br.com.auradevcode.vedra.core

/** Quando um app protegido, já desbloqueado, volta a pedir a credencial (RF-07). */
enum class RelockMode { IMMEDIATE, AFTER_TIMEOUT, SCREEN_OFF }

data class RelockPolicy(
    val mode: RelockMode = RelockMode.IMMEDIATE,
    val timeoutMs: Long = 0,
) {
    companion object {
        /** Padrão: 1 min fora do app. "Imediatamente" incomodava no dia a dia (ADR-015). */
        val Default = RelockPolicy(RelockMode.AFTER_TIMEOUT, 60_000L)
        val TimeoutOptionsMs = listOf(10_000L, 30_000L, 60_000L, 5 * 60_000L, 15 * 60_000L, 30 * 60_000L)
    }
}
