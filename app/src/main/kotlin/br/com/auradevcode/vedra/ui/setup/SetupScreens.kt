package br.com.auradevcode.vedra.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import br.com.auradevcode.vedra.PinResult
import br.com.auradevcode.vedra.R
import br.com.auradevcode.vedra.container
import br.com.auradevcode.vedra.core.PinRules
import br.com.auradevcode.vedra.ui.BiometricOutcome
import br.com.auradevcode.vedra.ui.Biometrics
import br.com.auradevcode.vedra.ui.components.BrandHeader
import br.com.auradevcode.vedra.ui.components.PinPad
import br.com.auradevcode.vedra.ui.components.VedraIcon
import br.com.auradevcode.vedra.ui.formatCountdown
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
private fun ScreenColumn(content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) { content() }
    }
}

@Composable
fun WelcomeScreen(onStart: () -> Unit) = ScreenColumn {
    BrandHeader()
    Spacer(Modifier.height(8.dp))
    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall)
    Text(stringResource(R.string.slogan), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(36.dp))
    Point(Icons.Outlined.Bolt, stringResource(R.string.welcome_point_fast))
    Point(Icons.Outlined.WifiOff, stringResource(R.string.welcome_point_private))
    Point(Icons.Outlined.Fingerprint, stringResource(R.string.welcome_point_simple))
    Spacer(Modifier.height(40.dp))
    Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(stringResource(R.string.welcome_start)) }
}

@Composable
private fun Point(icon: ImageVector, text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(14.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Criar e confirmar o PIN (RF-02). */
@Composable
fun PinSetupScreen(onDone: (String) -> Unit) = ScreenColumn {
    var length by remember { mutableIntStateOf(PinRules.DEFAULT_LENGTH) }
    var first by remember { mutableStateOf<String?>(null) }
    var pendingWeak by remember { mutableStateOf<String?>(null) }
    var errorSignal by remember { mutableIntStateOf(0) }
    var clearSignal by remember { mutableIntStateOf(0) }
    var mismatch by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    VedraIcon(56.dp)
    Spacer(Modifier.height(20.dp))
    Text(
        stringResource(if (first == null) R.string.setup_create_pin else R.string.setup_confirm_pin),
        style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        if (mismatch) stringResource(R.string.setup_pin_mismatch)
        else stringResource(R.string.setup_create_pin_hint, PinRules.MIN_LENGTH, PinRules.MAX_LENGTH),
        style = MaterialTheme.typography.bodyMedium,
        color = if (mismatch) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    if (first == null) {
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(4, 6, 8).forEach { n ->
                FilterChip(selected = length == n, onClick = { length = n }, label = { Text(stringResource(R.string.setup_pin_length, n)) })
            }
        }
    }
    Spacer(Modifier.height(28.dp))
    PinPad(
        length = length,
        enabled = !saving,
        errorSignal = errorSignal,
        clearSignal = clearSignal,
        onComplete = { pin ->
            val firstPin = first
            when {
                firstPin == null && PinRules.isWeak(pin) -> pendingWeak = pin
                firstPin == null -> { first = pin; mismatch = false; clearSignal++ }
                firstPin == pin -> { saving = true; onDone(pin) }
                else -> { first = null; mismatch = true; errorSignal++ }
            }
        },
    )
    Spacer(Modifier.height(16.dp))
    Text(
        stringResource(R.string.setup_no_recovery),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )

    pendingWeak?.let { weak ->
        AlertDialog(
            onDismissRequest = { pendingWeak = null; clearSignal++ },
            title = { Text(stringResource(R.string.setup_create_pin)) },
            text = { Text(stringResource(R.string.setup_pin_weak)) },
            confirmButton = {
                TextButton(onClick = { first = weak; pendingWeak = null; clearSignal++ }) {
                    Text(stringResource(R.string.setup_pin_weak_use))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingWeak = null; clearSignal++ }) { Text(stringResource(R.string.setup_pin_weak_change)) }
            },
        )
    }
}

@Composable
fun BiometricOfferScreen(onUse: () -> Unit, onSkip: () -> Unit) = ScreenColumn {
    Icon(Icons.Outlined.Fingerprint, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(88.dp))
    Spacer(Modifier.height(24.dp))
    Text(stringResource(R.string.setup_biometric_title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
    Spacer(Modifier.height(8.dp))
    Text(
        stringResource(R.string.setup_biometric_body),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(36.dp))
    Button(onClick = onUse, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(stringResource(R.string.setup_biometric_use)) }
    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(stringResource(R.string.setup_biometric_skip)) }
}

/** Abrir o próprio Vedra também pede a credencial (RF-09). */
@Composable
fun UnlockGateScreen(biometricEnabled: Boolean, onUnlocked: () -> Unit) = ScreenColumn {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val app = context.container
    val scope = rememberCoroutineScope()
    var length by remember { mutableIntStateOf(PinRules.DEFAULT_LENGTH) }
    var errorSignal by remember { mutableIntStateOf(0) }
    var lockout by remember { mutableStateOf(app.unlocker.remainingLockoutMs()) }
    var wrong by remember { mutableStateOf(false) }
    val title = stringResource(R.string.biometric_title_vedra)

    fun bio() = Biometrics.authenticate(activity, title) { if (it == BiometricOutcome.SUCCESS) { app.unlocker.onSuccess(); onUnlocked() } }

    LaunchedEffect(Unit) {
        length = app.credentials.pinLength().takeIf { it > 0 } ?: PinRules.DEFAULT_LENGTH
        if (biometricEnabled && lockout == 0L) bio()
    }
    LaunchedEffect(lockout > 0) {
        while (lockout > 0) { delay(1_000); lockout = app.unlocker.remainingLockoutMs() }
    }

    BrandHeader()
    Text(
        when {
            lockout > 0 -> stringResource(R.string.lock_wait, formatCountdown(lockout))
            wrong -> stringResource(R.string.lock_wrong_pin)
            else -> stringResource(R.string.lock_enter_pin)
        },
        color = if (lockout > 0 || wrong) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(28.dp))
    PinPad(
        length = length,
        enabled = lockout == 0L,
        errorSignal = errorSignal,
        onBiometric = if (biometricEnabled) ({ bio() }) else null,
        onComplete = { pin ->
            scope.launch {
                when (val r = app.unlocker.tryPin(pin)) {
                    PinResult.Success -> onUnlocked()
                    is PinResult.Wrong -> { wrong = true; errorSignal++; lockout = r.lockoutMs }
                    is PinResult.LockedOut -> { errorSignal++; lockout = r.remainingMs }
                }
            }
        },
    )
}
