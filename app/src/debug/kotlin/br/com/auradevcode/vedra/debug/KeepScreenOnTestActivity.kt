package br.com.auradevcode.vedra.debug

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

/**
 * Só no build de depuração: simula um player de vídeo que pede para manter a tela acesa.
 * Serve para provar que a cortina invisível não anula o FLAG_KEEP_SCREEN_ON de outros apps.
 *
 * adb shell am start -n br.com.auradevcode.vedra.dev/br.com.auradevcode.vedra.debug.KeepScreenOnTestActivity
 * adb shell dumpsys window | grep mHoldScreenWindow
 */
class KeepScreenOnTestActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(
            TextView(this).apply {
                text = "KEEP_SCREEN_ON"
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.DKGRAY)
            },
        )
    }
}
