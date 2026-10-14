package br.com.auradevcode.vedra.guard

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView

/**
 * A cortina: janela opaca sobre o app protegido, exibida no mesmo instante do evento (ADR-003).
 *
 * A janela é anexada uma única vez, quando o serviço conecta, invisível e sem receber toques.
 * Mostrar e esconder só troca a transparência e os flags: não cria superfície nova no caminho crítico (RNF-01).
 */
internal class CurtainController(service: AccessibilityService, secure: Boolean) {
    private val windowManager = service.getSystemService(WindowManager::class.java)

    private val view: View = FrameLayout(service).apply {
        setBackgroundColor(Color.rgb(0x0A, 0x0A, 0x0A))
        val size = (96 * resources.displayMetrics.density).toInt()
        addView(
            ImageView(service).apply { setImageResource(R.drawable.vedra_curtain_glyph) },
            FrameLayout.LayoutParams(size, size, Gravity.CENTER),
        )
    }

    private val baseFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
        (if (secure) WindowManager.LayoutParams.FLAG_SECURE else 0)

    private val hiddenFlags = baseFlags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        hiddenFlags,
        // TRANSLUCENT, nunca OPAQUE: uma janela opaca do tamanho da tela, mesmo com alpha 0,
        // faz o sistema achar que tudo embaixo está coberto e ignorar o FLAG_KEEP_SCREEN_ON
        // dos outros apps (a tela apagava no meio de vídeos). O fundo da view já é opaco.
        PixelFormat.TRANSLUCENT,
    ).apply {
        alpha = 0f
        windowAnimations = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    private var attached = false

    var isShowing = false
        private set

    fun attach() {
        if (attached) return
        windowManager.addView(view, params)
        attached = true
    }

    fun show() {
        if (isShowing) return
        if (!attached) attach()
        params.alpha = 1f
        params.flags = baseFlags
        windowManager.updateViewLayout(view, params)
        isShowing = true
    }

    fun hide() {
        if (!isShowing) return
        params.alpha = 0f
        params.flags = hiddenFlags
        windowManager.updateViewLayout(view, params)
        isShowing = false
    }

    fun detach() {
        if (!attached) return
        windowManager.removeViewImmediate(view)
        attached = false
        isShowing = false
    }
}
