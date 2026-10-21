package br.com.auradevcode.vedra.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.auradevcode.vedra.R
import br.com.auradevcode.vedra.core.RelockMode
import br.com.auradevcode.vedra.core.RelockPolicy
import br.com.auradevcode.vedra.data.VedraSettings
import br.com.auradevcode.vedra.ui.formatDuration

@Composable
fun SettingsScreen(
    settings: VedraSettings,
    biometricAvailable: Boolean,
    version: String,
    onRelock: (RelockPolicy) -> Unit,
    onBiometric: (Boolean) -> Unit,
    onChangePin: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val options = buildList {
        add(RelockPolicy(RelockMode.IMMEDIATE) to stringResource(R.string.relock_immediate))
        RelockPolicy.TimeoutOptionsMs.forEach { ms ->
            add(RelockPolicy(RelockMode.AFTER_TIMEOUT, ms) to stringResource(R.string.relock_after, formatDuration(context, ms)))
        }
        add(RelockPolicy(RelockMode.SCREEN_OFF) to stringResource(R.string.relock_screen_off))
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState())) {
            Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back)) }
                Text(stringResource(R.string.settings), style = MaterialTheme.typography.titleLarge)
            }

            Text(
                stringResource(R.string.settings_relock),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 4.dp),
            )
            options.forEach { (policy, label) ->
                val selected = settings.relock == policy
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(selected = selected, onClick = { onRelock(policy) })
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected, onClick = { onRelock(policy) })
                    Spacer(Modifier.width(4.dp))
                    Text(label, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Text(
                stringResource(R.string.relock_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_biometric), style = MaterialTheme.typography.bodyLarge)
                    if (!biometricAvailable) {
                        Text(
                            stringResource(R.string.setup_biometric_unavailable),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Switch(
                    checked = settings.biometricEnabled && biometricAvailable,
                    enabled = biometricAvailable,
                    onCheckedChange = onBiometric,
                )
            }
            TextButton(onClick = onChangePin, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(stringResource(R.string.settings_change_pin))
            }

            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.settings_about, version),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
        }
    }
}
