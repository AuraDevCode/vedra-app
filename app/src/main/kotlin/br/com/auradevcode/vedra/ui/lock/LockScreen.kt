package br.com.auradevcode.vedra.ui.lock

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import br.com.auradevcode.vedra.R
import br.com.auradevcode.vedra.ui.components.Aura
import br.com.auradevcode.vedra.ui.components.PinPad
import br.com.auradevcode.vedra.ui.components.VedraIcon
import br.com.auradevcode.vedra.ui.formatCountdown
import kotlinx.coroutines.delay

@Composable
fun LockScreen(
    appLabel: String,
    appIcon: Drawable?,
    pinLength: Int,
    message: String?,
    errorSignal: Int,
    lockoutMs: Long,
    onLockoutTick: () -> Unit,
    onPin: (String) -> Unit,
    onBiometric: (() -> Unit)?,
) {
    var showForgot by remember { mutableStateOf(false) }

    val currentLockout by rememberUpdatedState(lockoutMs)
    LaunchedEffect(lockoutMs > 0) {
        while (currentLockout > 0) {
            delay(1_000)
            onLockoutTick()
        }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            Aura(Modifier.fillMaxSize(), alpha = 0.18f)
            Column(
                Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val bitmap = remember(appIcon) { appIcon?.toBitmap(192, 192)?.asImageBitmap() }
                if (bitmap != null) {
                    Image(bitmap, contentDescription = null, modifier = Modifier.size(72.dp))
                } else {
                    VedraIcon(72.dp)
                }
                Spacer(Modifier.height(14.dp))
                Text(appLabel, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                val status = when {
                    lockoutMs > 0 -> stringResource(R.string.lock_wait, formatCountdown(lockoutMs))
                    message != null -> message
                    else -> stringResource(R.string.lock_enter_pin)
                }
                Text(
                    status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (lockoutMs > 0 || message != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(32.dp))
                PinPad(
                    length = pinLength,
                    onComplete = onPin,
                    enabled = lockoutMs == 0L,
                    errorSignal = errorSignal,
                    onBiometric = onBiometric,
                )
                Spacer(Modifier.height(20.dp))
                TextButton(onClick = { showForgot = true }) { Text(stringResource(R.string.lock_forgot)) }
            }
        }
    }

    if (showForgot) {
        AlertDialog(
            onDismissRequest = { showForgot = false },
            confirmButton = { TextButton(onClick = { showForgot = false }) { Text(stringResource(R.string.ok)) } },
            title = { Text(stringResource(R.string.lock_forgot)) },
            text = { Text(stringResource(R.string.lock_forgot_body)) },
        )
    }
}
