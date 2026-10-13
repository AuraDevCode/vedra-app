package br.com.auradevcode.vedra.ui

import android.view.Window
import android.view.WindowManager
import br.com.auradevcode.vedra.BuildConfig

/**
 * `FLAG_SECURE` em todas as telas do Vedra no release (RNF-18).
 * No build de depuração fica desligado para permitir capturas e gravações de teste (specs/10).
 */
fun Window.applySecureFlag() {
    if (!BuildConfig.DEBUG) addFlags(WindowManager.LayoutParams.FLAG_SECURE)
}
