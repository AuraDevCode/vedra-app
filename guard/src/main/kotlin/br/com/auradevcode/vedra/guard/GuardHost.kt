package br.com.auradevcode.vedra.guard

import android.content.Context
import br.com.auradevcode.vedra.core.LockEngine

/** Implementado pela `Application`: liga o serviço ao resto do app sem dependência circular. */
interface GuardHost {
    val lockEngine: LockEngine

    /** `FLAG_SECURE` na cortina. Desligado só no build de depuração, para testes gravados. */
    val secureWindows: Boolean

    fun launchLock(context: Context, packageName: String)
}
