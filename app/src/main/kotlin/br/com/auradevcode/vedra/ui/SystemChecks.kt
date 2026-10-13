package br.com.auradevcode.vedra.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import br.com.auradevcode.vedra.guard.VedraAccessibilityService

data class Health(
    val serviceEnabled: Boolean,
    val serviceRunning: Boolean,
    val batteryUnrestricted: Boolean,
    val backgroundRestricted: Boolean,
    val protectedCount: Int,
) {
    enum class Level { PROTECTED, ATTENTION, UNPROTECTED }

    val level: Level
        get() = when {
            !serviceEnabled || !serviceRunning || backgroundRestricted || protectedCount == 0 -> Level.UNPROTECTED
            !batteryUnrestricted -> Level.ATTENTION
            else -> Level.PROTECTED
        }
}

/** Verificações de saúde (specs/05). Chamadas em onResume, nunca no caminho do serviço. */
object SystemChecks {
    /** Ação oculta no SDK, mas disponível no Android 11+ para o próprio serviço do app. */
    private const val ACTION_ACCESSIBILITY_DETAILS = "android.settings.ACCESSIBILITY_DETAILS_SETTINGS"

    fun health(context: Context, serviceRunning: Boolean, protectedCount: Int) = Health(
        serviceEnabled = isServiceEnabled(context),
        serviceRunning = serviceRunning,
        batteryUnrestricted = context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName),
        backgroundRestricted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            context.getSystemService(ActivityManager::class.java).isBackgroundRestricted,
        protectedCount = protectedCount,
    )

    fun isServiceEnabled(context: Context): Boolean {
        val manager = context.getSystemService(AccessibilityManager::class.java)
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK).any {
            it.resolveInfo.serviceInfo.packageName == context.packageName &&
                it.resolveInfo.serviceInfo.name == VedraAccessibilityService::class.java.name
        }
    }

    fun isSamsung() = Build.MANUFACTURER.equals("samsung", ignoreCase = true)

    fun openAccessibilitySettings(context: Context) {
        val component = ComponentName(context, VedraAccessibilityService::class.java).flattenToString()
        val candidates = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(Intent(ACTION_ACCESSIBILITY_DETAILS).putExtra(Intent.EXTRA_COMPONENT_NAME, component))
            }
            add(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        launchFirst(context, candidates)
    }

    fun openBatterySettings(context: Context) {
        val details = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        val list = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        // Na Samsung, "Apps → Vedra → Bateria → Sem restrições" é o caminho mais claro.
        launchFirst(context, if (isSamsung()) listOf(details, list) else listOf(list, details))
    }

    fun openAppDetails(context: Context) {
        launchFirst(context, listOf(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))))
    }

    private fun launchFirst(context: Context, intents: List<Intent>) {
        for (intent in intents) {
            val ok = runCatching {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }.isSuccess
            if (ok) return
        }
    }
}
