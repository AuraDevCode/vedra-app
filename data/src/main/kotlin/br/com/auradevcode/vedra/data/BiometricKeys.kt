package br.com.auradevcode.vedra.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Chave usada no `CryptoObject` do BiometricPrompt (RF-03, A10).
 * Uma nova digital cadastrada no aparelho invalida a chave, e o Vedra passa a exigir o PIN.
 */
object BiometricKeys {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "vedra_bio"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    sealed interface CipherResult {
        data class Ready(val cipher: Cipher) : CipherResult

        /** Digital nova cadastrada: a chave foi apagada e precisa ser recriada após o PIN. */
        data object Invalidated : CipherResult
        data object Unavailable : CipherResult
    }

    fun createKey(): Boolean = runCatching {
        deleteKey()
        val spec = KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .build()
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply { init(spec) }.generateKey()
        true
    }.getOrDefault(false)

    fun cipher(): CipherResult {
        val key = runCatching {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }.getKey(ALIAS, null) as? SecretKey
        }.getOrNull() ?: return CipherResult.Unavailable
        return try {
            CipherResult.Ready(Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key) })
        } catch (e: KeyPermanentlyInvalidatedException) {
            deleteKey()
            CipherResult.Invalidated
        } catch (e: Exception) {
            CipherResult.Unavailable
        }
    }

    fun deleteKey() {
        runCatching { KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }.deleteEntry(ALIAS) }
    }
}
