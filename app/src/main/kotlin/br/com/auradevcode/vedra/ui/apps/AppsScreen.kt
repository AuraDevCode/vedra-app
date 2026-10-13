package br.com.auradevcode.vedra.ui.apps

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import br.com.auradevcode.vedra.R
import br.com.auradevcode.vedra.data.AppCatalog
import br.com.auradevcode.vedra.data.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Escolher apps protegidos (RF-04). */
@Composable
fun AppsScreen(
    catalog: AppCatalog,
    protectedPackages: Set<String>,
    onToggle: (String, Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val apps by produceState<List<InstalledApp>?>(null) { value = catalog.launcherApps() }
    var query by remember { mutableStateOf("") }
    // Ordem fixa enquanto a tela está aberta: ligar uma chave não faz a lista pular.
    val initiallyProtected = remember { protectedPackages }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Row(Modifier.padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back)) }
                Text(stringResource(R.string.apps_title), style = MaterialTheme.typography.titleLarge)
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                placeholder = { Text(stringResource(R.string.apps_search)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.medium,
            )

            val list = apps
            if (list == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                return@Column
            }
            val filtered = list.filter { query.isBlank() || it.label.contains(query.trim(), ignoreCase = true) }
            val suggested = filtered.filter { it.packageName in AppCatalog.SUGGESTED }
            // Protegidos primeiro, depois ordem alfabética (já vem do catálogo).
            val others = filtered.filter { it.packageName !in AppCatalog.SUGGESTED }
                .sortedByDescending { it.packageName in initiallyProtected }

            LazyColumn(Modifier.fillMaxSize()) {
                if (suggested.isNotEmpty()) {
                    item { SectionTitle(stringResource(R.string.apps_suggested)) }
                    items(suggested, key = { "s_" + it.packageName }) { app ->
                        AppRow(app, catalog, app.packageName in protectedPackages, onToggle)
                    }
                }
                item { SectionTitle(stringResource(R.string.apps_all)) }
                items(others, key = { it.packageName }) { app ->
                    AppRow(app, catalog, app.packageName in protectedPackages, onToggle)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 6.dp),
    )
}

@Composable
private fun AppRow(app: InstalledApp, catalog: AppCatalog, checked: Boolean, onToggle: (String, Boolean) -> Unit) {
    val icon by produceState<ImageBitmap?>(null, app.packageName) {
        value = withContext(Dispatchers.IO) { catalog.icon(app.packageName)?.toBitmap(96, 96)?.asImageBitmap() }
    }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp)) { icon?.let { Image(it, null, Modifier.fillMaxSize()) } }
        Spacer(Modifier.width(16.dp))
        Text(app.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = { onToggle(app.packageName, it) })
    }
}
