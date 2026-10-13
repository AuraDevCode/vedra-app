package br.com.auradevcode.vedra.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale

data class InstalledApp(val packageName: String, val label: String)

/** Apps com ícone no launcher, sem `QUERY_ALL_PACKAGES` (P5). */
class AppCatalog(context: Context) {
    private val appContext = context.applicationContext
    private val pm: PackageManager = appContext.packageManager

    suspend fun launcherApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val collator = Collator.getInstance(Locale.getDefault())
        pm.queryIntentActivities(intent, 0)
            .asSequence()
            .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
            .filter { (pkg, _) -> pkg != appContext.packageName }
            .distinctBy { it.first }
            .map { (pkg, label) -> InstalledApp(pkg, label) }
            .sortedWith { a, b -> collator.compare(a.label, b.label) }
            .toList()
    }

    fun icon(packageName: String): Drawable? = runCatching { pm.getApplicationIcon(packageName) }.getOrNull()

    fun label(packageName: String): String = runCatching {
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    }.getOrDefault(packageName)

    companion object {
        /** Apps sensíveis sugeridos no primeiro uso (RF-04). */
        val SUGGESTED = setOf(
            "com.whatsapp",
            "com.whatsapp.w4b",
            "org.telegram.messenger",
            "com.sec.android.gallery3d",
            "com.google.android.apps.photos",
            "com.google.android.gm",
            "com.samsung.android.app.notes",
            "com.sec.android.app.myfiles",
            "com.nu.production",
            "com.itau",
            "br.com.bb.android",
            "com.bradesco",
            "br.com.intermedium",
            "com.santander.app",
            "br.com.gabba.Caixa",
            "com.picpay",
            "com.mercadopago.wallet",
            "com.instagram.android",
            "com.facebook.orca",
        )
    }
}
