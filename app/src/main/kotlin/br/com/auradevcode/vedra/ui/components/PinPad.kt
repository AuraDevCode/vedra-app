package br.com.auradevcode.vedra.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import br.com.auradevcode.vedra.R
import br.com.auradevcode.vedra.ui.theme.PinDigitStyle
import kotlin.math.roundToInt

/**
 * Teclado de PIN próprio (RF-06): não usa o teclado do sistema.
 * Chama [onComplete] assim que [length] dígitos são digitados.
 *
 * @param errorSignal muda de valor a cada erro, para tremer os pontos e limpar o PIN.
 */
@Composable
fun PinPad(
    length: Int,
    onComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    errorSignal: Int = 0,
    clearSignal: Int = 0,
    onBiometric: (() -> Unit)? = null,
) {
    var pin by remember { mutableStateOf("") }
    val shake = remember { Animatable(0f) }
    val view = LocalView.current

    LaunchedEffect(errorSignal) {
        if (errorSignal == 0) return@LaunchedEffect
        pin = ""
        view.performHapticFeedback(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS,
        )
        shake.animateTo(0f, keyframes {
            durationMillis = 300
            -18f at 50; 16f at 100; -12f at 150; 8f at 200; -4f at 250
        })
    }
    LaunchedEffect(length, clearSignal) { pin = "" }

    fun press(digit: Char) {
        if (!enabled || pin.length >= length) return
        pin += digit
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        if (pin.length == length) {
            val done = pin
            onComplete(done)
        }
    }

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        val progress = stringResource(R.string.pin_progress, pin.length, length)
        Row(
            Modifier
                .offset { IntOffset(shake.value.roundToInt(), 0) }
                .semantics { contentDescription = progress },
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            repeat(length) { i ->
                val filled = i < pin.length
                Box(
                    Modifier
                        .size(14.dp)
                        .then(
                            if (filled) Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                            else Modifier.border(1.5.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape),
                        ),
                )
            }
        }
        Spacer(Modifier.height(36.dp))

        val rows = listOf("123", "456", "789")
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                row.forEach { d -> Key(enabled, d.toString()) { press(d) } }
            }
            Spacer(Modifier.height(18.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onBiometric != null) {
                IconKey(stringResource(R.string.key_biometric), onBiometric) {
                    Icon(Icons.Outlined.Fingerprint, null, Modifier.size(30.dp), tint = MaterialTheme.colorScheme.secondary)
                }
            } else {
                Spacer(Modifier.width(KEY_SIZE))
            }
            Key(enabled, "0") { press('0') }
            IconKey(stringResource(R.string.key_delete), { if (pin.isNotEmpty()) pin = pin.dropLast(1) }) {
                Icon(Icons.AutoMirrored.Outlined.Backspace, null, Modifier.size(26.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private val KEY_SIZE = 76.dp

@Composable
private fun Key(enabled: Boolean, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.size(KEY_SIZE),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                style = PinDigitStyle,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun IconKey(description: String, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier
            .size(KEY_SIZE)
            .semantics { contentDescription = description },
    ) {
        Box(contentAlignment = Alignment.Center) { icon() }
    }
}
