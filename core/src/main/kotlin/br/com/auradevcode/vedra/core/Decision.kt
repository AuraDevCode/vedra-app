package br.com.auradevcode.vedra.core

sealed interface Decision {
    /** Janela passageira (teclado, UI do sistema, a própria tela de bloqueio): não muda nada. */
    data object Ignore : Decision

    /** App livre ou sessão liberada: a cortina, se estiver na tela, pode sair. */
    data object Allow : Decision

    /** App protegido sem sessão: cobrir e pedir a credencial. */
    data class Lock(val packageName: String) : Decision
}
