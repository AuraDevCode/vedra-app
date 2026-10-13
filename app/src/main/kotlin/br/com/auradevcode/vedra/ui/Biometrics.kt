package br.com.auradevcode.vedra.ui

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import br.com.auradevcode.vedra.R
import br.com.auradevcode.vedra.data.BiometricKeys

enum class BiometricOutcome { SUCCESS, INVALIDATED, CANCELLED, UNAVAILABLE }

object Biometrics {
    fun isAvailable(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    /**
     * Pede a digital com `CryptoObject` (RF-03). Se uma digital nova foi cadastrada,
     * a chave já foi invalidada pelo Keystore e o resultado é [BiometricOutcome.INVALIDATED].
     */
    fun authenticate(activity: FragmentActivity, title: String, onResult: (BiometricOutcome) -> Unit) {
        val cipher = when (val result = BiometricKeys.cipher()) {
            is BiometricKeys.CipherResult.Ready -> result.cipher
            BiometricKeys.CipherResult.Invalidated -> return onResult(BiometricOutcome.INVALIDATED)
            BiometricKeys.CipherResult.Unavailable -> return onResult(BiometricOutcome.UNAVAILABLE)
        }
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val ok = runCatching { result.cryptoObject?.cipher?.doFinal(ByteArray(16)) }.getOrNull() != null
                    onResult(if (ok) BiometricOutcome.SUCCESS else BiometricOutcome.CANCELLED)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onResult(BiometricOutcome.CANCELLED)
                }
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setNegativeButtonText(activity.getString(R.string.biometric_negative))
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .setConfirmationRequired(false)
            .build()
        prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
    }

    /** Cria a chave e confirma com uma digital real antes de ligar o recurso. */
    fun enroll(activity: FragmentActivity, onResult: (Boolean) -> Unit) {
        if (!BiometricKeys.createKey()) return onResult(false)
        authenticate(activity, activity.getString(R.string.setup_biometric_title)) { onResult(it == BiometricOutcome.SUCCESS) }
    }
}
