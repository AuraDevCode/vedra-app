package br.com.auradevcode.vedra.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.auradevcode.vedra.R
import br.com.auradevcode.vedra.guard.LastLock
import br.com.auradevcode.vedra.ui.Health
import br.com.auradevcode.vedra.ui.components.Aura
import br.com.auradevcode.vedra.ui.components.VedraIcon
import br.com.auradevcode.vedra.ui.formatMicros
import br.com.auradevcode.vedra.ui.theme.VedraColors

/** Painel de saúde (RF-10). */
@Composable
fun HomeScreen(
    health: Health,
    lastLock: LastLock?,
    lastLockLabel: String?,
    onEnableService: () -> Unit,
    onOpenAppDetails: () -> Unit,
    onFixBattery: () -> Unit,
    onApps: () -> Unit,
    onSettings: () -> Unit,
) {
    var showDisclosure by remember { mutableStateOf(false) }

    val (levelColor, levelText) = when (health.level) {
        Health.Level.PROTECTED -> VedraColors.Protected to stringResource(R.string.status_protected)
        Health.Level.ATTENTION -> VedraColors.Attention to stringResource(R.string.status_attention)
        Health.Level.UNPROTECTED -> VedraColors.Unprotected to stringResource(R.string.status_unprotected)
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, stringResource(R.string.settings)) }
            }

            Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                val pulse = if (health.level == Health.Level.PROTECTED) {
                    rememberInfiniteTransition(label = "aura").animateFloat(
                        initialValue = 0.92f,
                        targetValue = 1.06f,
                        animationSpec = infiniteRepeatable(tween(2_000), RepeatMode.Reverse),
                        label = "pulse",
                    ).value
                } else 1f
                Aura(Modifier.size(240.dp).scale(pulse), tint = levelColor, alpha = 0.3f)
                VedraIcon(104.dp)
            }
            Text(levelText, style = MaterialTheme.typography.headlineSmall, color = levelColor, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(
                pluralStringResource(R.plurals.status_apps_count, health.protectedCount, health.protectedCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(24.dp))

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(vertical = 6.dp)) {
                    when {
                        !health.serviceEnabled -> CheckRow(false, stringResource(R.string.check_service_off), stringResource(R.string.check_restricted_settings)) { showDisclosure = true }
                        !health.serviceRunning -> CheckRow(false, stringResource(R.string.check_service_stuck), null) { showDisclosure = true }
                        else -> CheckRow(true, stringResource(R.string.check_service_ok), null, null)
                    }
                    if (health.backgroundRestricted) CheckRow(false, stringResource(R.string.check_bg_restricted), null, onOpenAppDetails)
                    if (health.batteryUnrestricted) {
                        CheckRow(true, stringResource(R.string.check_battery_ok), null, null)
                    } else {
                        CheckRow(null, stringResource(R.string.check_battery_off), null, onFixBattery)
                    }
                    if (health.protectedCount == 0) CheckRow(false, stringResource(R.string.check_apps_none), null, onApps)
                }
            }

            if (lastLock != null && lastLockLabel != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.last_lock, lastLockLabel, formatMicros(lastLock.curtainMicros)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            Spacer(Modifier.height(24.dp))
            Button(onClick = onApps, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(stringResource(R.string.manage_apps)) }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDisclosure) {
        AccessibilityDisclosure(
            onAccept = { showDisclosure = false; onEnableService() },
            onDecline = { showDisclosure = false },
        )
    }
}

/** @param ok true = verde, false = vermelho, null = amarelo. */
@Composable
private fun CheckRow(ok: Boolean?, title: String, detail: String?, onFix: (() -> Unit)?) {
    val (icon, tint) = when (ok) {
        true -> Icons.Outlined.CheckCircle to VedraColors.Protected
        false -> Icons.Outlined.ErrorOutline to VedraColors.Unprotected
        null -> Icons.Outlined.WarningAmber to VedraColors.Attention
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (detail != null) Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (onFix != null) TextButton(onClick = onFix) { Text(stringResource(R.string.fix)) }
    }
}

/** Aviso em destaque exigido pela Play antes de ativar a acessibilidade (RF-01, specs/09). */
@Composable
private fun AccessibilityDisclosure(onAccept: () -> Unit, onDecline: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDecline,
        title = { Text(stringResource(R.string.disclosure_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.disclosure_body))
                Text(stringResource(R.string.disclosure_sees), color = Color.Unspecified)
                Text(stringResource(R.string.disclosure_not_sees))
                Text(stringResource(R.string.disclosure_storage))
                Text(stringResource(R.string.disclosure_off), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { Button(onClick = onAccept) { Text(stringResource(R.string.disclosure_accept)) } },
        dismissButton = { TextButton(onClick = onDecline) { Text(stringResource(R.string.disclosure_decline)) } },
    )
}
