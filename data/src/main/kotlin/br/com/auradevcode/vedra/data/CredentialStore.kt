package br.com.auradevcode.vedra.data

import android.content.Context
import android.os.SystemClock
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private val Context.credentialStore by preferencesDataStore(name = "vedra_credential")

/**
 * Guarda o PIN só como `HMAC_keystore(PBKDF2(pin, salt))` (ADR-010, A14).
 * A chave HMAC fica no Android Keystore e não sai do aparelho.
 */
class CredentialStore(context: Context) {
    private val store = context.applicationContext.credentialStore

    val hasCredential = store.data.map { it[HASH] != null }

    suspend fun pinLength(): Int = store.data.first()[LENGTH] ?: 0

    suspend fun setPin(pin: String): Unit = withContext(Dispatchers.Default) {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val iterations = calibrateIterations()
        val hash = derive(pin, salt, iterations)
        store.edit {
            it[SALT] = salt.b64()
            it[HASH] = hash.b64()
            it[ITERATIONS] = iterations
            it[LENGTH] = pin.length
            it[VERSION] = 1
        }
        Unit
    }

    suspend fun verify(pin: String): Boolean = withContext(Dispatchers.Default) {
        val prefs = store.data.first()
        val salt = prefs[SALT]?.unb64() ?: return@withContext false
        val expected = prefs[HASH]?.unb64() ?: return@withContext false
        val iterations = prefs[ITERATIONS] ?: return@withContext false
        MessageDigest.isEqual(expected, derive(pin, salt, iterations))
    }

    private fun derive(pin: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, 256)
        val derived = try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
        return Mac.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256).run {
            init(pepperKey())
            doFinal(derived)
        }
    }

    /** Ajusta as iterações para cerca de 150 ms neste aparelho, nunca abaixo do mínimo. */
    private fun calibrateIterations(): Int {
        val probe = 20_000
        val start = SystemClock.elapsedRealtime()
        val spec = PBEKeySpec(CharArray(6) { '0' }, ByteArray(SALT_BYTES), probe, 256)
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec)
        val elapsed = (SystemClock.elapsedRealtime() - start).coerceAtLeast(1)
        val target = (probe.toLong() * TARGET_MS / elapsed).toInt()
        return target.coerceIn(MIN_ITERATIONS, MAX_ITERATIONS)
    }

    private fun pepperKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(PEPPER_ALIAS, null) as? SecretKey)?.let { return it }

        fun generate(strongBox: Boolean): SecretKey {
            val builder = KeyGenParameterSpec.Builder(PEPPER_ALIAS, KeyProperties.PURPOSE_SIGN)
            if (strongBox && android.os.Build.VERSION.SDK_INT >= 28) builder.setIsStrongBoxBacked(true)
            return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, ANDROID_KEYSTORE)
                .apply { init(builder.build()) }
                .generateKey()
        }
        return try {
            generate(strongBox = true)
        } catch (e: StrongBoxUnavailableException) {
            generate(strongBox = false)
        } catch (e: Exception) {
            generate(strongBox = false)
        }
    }

    private fun ByteArray.b64() = Base64.encodeToString(this, Base64.NO_WRAP)
    private fun String.unb64() = Base64.decode(this, Base64.NO_WRAP)

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val PEPPER_ALIAS = "vedra_pin_pepper"
        const val SALT_BYTES = 16
        const val TARGET_MS = 150L
        const val MIN_ITERATIONS = 210_000
        const val MAX_ITERATIONS = 2_000_000

        val SALT = stringPreferencesKey("salt")
        val HASH = stringPreferencesKey("hash")
        val ITERATIONS = intPreferencesKey("iterations")
        val LENGTH = intPreferencesKey("pin_length")
        val VERSION = intPreferencesKey("version")
    }
}
