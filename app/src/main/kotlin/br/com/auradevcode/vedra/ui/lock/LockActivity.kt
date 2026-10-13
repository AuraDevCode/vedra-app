package br.com.auradevcode.vedra.ui.lock

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import br.com.auradevcode.vedra.PinResult
import br.com.auradevcode.vedra.R
import br.com.auradevcode.vedra.container
import br.com.auradevcode.vedra.guard.GuardStatus
import br.com.auradevcode.vedra.ui.BiometricOutcome
import br.com.auradevcode.vedra.ui.Biometrics
import br.com.auradevcode.vedra.ui.applySecureFlag
import br.com.auradevcode.vedra.ui.theme.VedraTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Desafio de PIN e digital sobre o app protegido (ADR-003). Aberta pelo serviço de acessibilidade.
 * Fecha sem desbloquear se o usuário for para o Início ou para outro app.
 */
class LockActivity : FragmentActivity() {

    private var target by mutableStateOf("")
    private var targetLabel by mutableStateOf("")
    private var targetIcon by mutableStateOf<Drawable?>(null)
    private var pinLength by mutableIntStateOf(6)
    private var biometricEnabled by mutableStateOf(false)
    private var message by mutableStateOf<String?>(null)
    private var errorSignal by mutableIntStateOf(0)
    private var lockoutMs by mutableStateOf(0L)

    private var unlocked = false
    private var biometricAutoShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.applySecureFlag()
        readTarget(intent)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goHome()
        })
        lifecycleScope.launch { GuardStatus.dismissLock.collect { if (!unlocked) finishQuietly() } }
        lifecycleScope.launch {
            val app = container
            pinLength = app.credentials.pinLength().takeIf { it > 0 } ?: 6
            biometricEnabled = app.settings.settings.first().biometricEnabled && Biometrics.isAvailable(this@LockActivity)
            lockoutMs = app.unlocker.remainingLockoutMs()
            maybeAutoBiometric()
        }

        setContent {
            VedraTheme {
                LockScreen(
                    appLabel = targetLabel,
                    appIcon = targetIcon,
                    pinLength = pinLength,
                    message = message,
                    errorSignal = errorSignal,
                    lockoutMs = lockoutMs,
                    onLockoutTick = { lockoutMs = container.unlocker.remainingLockoutMs() },
                    onPin = ::checkPin,
                    onBiometric = if (biometricEnabled) ({ showBiometric() }) else null,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readTarget(intent)
        message = null
        biometricAutoShown = false
        maybeAutoBiometric()
    }

    override fun onResume() {
        super.onResume()
        // Depois do primeiro quadro desta tela, a cortina do serviço pode sair.
        window.decorView.post { window.decorView.postOnAnimation { GuardStatus.onLockScreenShown(target) } }
    }

    override fun onPause() {
        super.onPause()
        GuardStatus.onLockScreenHidden()
    }

    private fun readTarget(intent: Intent) {
        target = intent.getStringExtra(EXTRA_PACKAGE).orEmpty()
        targetLabel = container.catalog.label(target)
        targetIcon = container.catalog.icon(target)
    }

    private fun maybeAutoBiometric() {
        if (biometricEnabled && !biometricAutoShown && lockoutMs == 0L) {
            biometricAutoShown = true
            showBiometric()
        }
    }

    private fun showBiometric() {
        Biometrics.authenticate(this, getString(R.string.biometric_title, targetLabel)) { outcome ->
            when (outcome) {
                BiometricOutcome.SUCCESS -> {
                    container.unlocker.onSuccess()
                    onUnlocked()
                }
                BiometricOutcome.INVALIDATED -> {
                    biometricEnabled = false
                    lifecycleScope.launch { container.settings.setBiometricEnabled(false) }
                    message = getString(R.string.lock_bio_invalidated)
                }
                else -> Unit
            }
        }
    }

    private fun checkPin(pin: String) {
        lifecycleScope.launch {
            when (val result = container.unlocker.tryPin(pin)) {
                PinResult.Success -> onUnlocked()
                is PinResult.Wrong -> {
                    errorSignal++
                    message = getString(R.string.lock_wrong_pin)
                    lockoutMs = result.lockoutMs
                }
                is PinResult.LockedOut -> {
                    errorSignal++
                    lockoutMs = result.remainingMs
                }
            }
        }
    }

    private fun onUnlocked() {
        unlocked = true
        container.lockEngine.onUnlocked(target)
        finishQuietly()
    }

    private fun goHome() {
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finishQuietly()
    }

    @Suppress("DEPRECATION")
    private fun finishQuietly() {
        finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            overridePendingTransition(0, 0)
        }
    }

    companion object {
        private const val EXTRA_PACKAGE = "package"

        fun start(context: Context, packageName: String) {
            val intent = Intent(context, LockActivity::class.java)
                .putExtra(EXTRA_PACKAGE, packageName)
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
                )
            context.startActivity(intent)
        }
    }
}
