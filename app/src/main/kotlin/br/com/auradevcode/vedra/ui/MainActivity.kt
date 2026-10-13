package br.com.auradevcode.vedra.ui

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import android.graphics.Color
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import br.com.auradevcode.vedra.BuildConfig
import br.com.auradevcode.vedra.container
import br.com.auradevcode.vedra.data.VedraSettings
import br.com.auradevcode.vedra.guard.GuardStatus
import br.com.auradevcode.vedra.ui.apps.AppsScreen
import br.com.auradevcode.vedra.ui.home.HomeScreen
import br.com.auradevcode.vedra.ui.settings.SettingsScreen
import br.com.auradevcode.vedra.ui.setup.BiometricOfferScreen
import br.com.auradevcode.vedra.ui.setup.PinSetupScreen
import br.com.auradevcode.vedra.ui.setup.UnlockGateScreen
import br.com.auradevcode.vedra.ui.setup.WelcomeScreen
import br.com.auradevcode.vedra.ui.theme.VedraTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private enum class Screen { LOADING, WELCOME, CREATE_PIN, BIOMETRIC, UNLOCK, HOME, APPS, SETTINGS, CHANGE_PIN }

class MainActivity : FragmentActivity() {

    private var screen by mutableStateOf(Screen.LOADING)
    private var resumeTick by mutableIntStateOf(0)

    /** Ida às Configurações do Android a pedido do próprio Vedra: não pede o PIN na volta. */
    private var skipRelockOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        window.applySecureFlag()

        lifecycleScope.launch {
            screen = if (container.credentials.hasCredential.first()) Screen.UNLOCK else Screen.WELCOME
        }

        setContent {
            VedraTheme {
                val app = container
                val settings by app.settings.settings.collectAsState(VedraSettings())
                val running by GuardStatus.running.collectAsState()
                val lastLock by GuardStatus.lastLock.collectAsState()

                when (screen) {
                    Screen.LOADING -> Unit
                    Screen.WELCOME -> WelcomeScreen(onStart = { screen = Screen.CREATE_PIN })
                    Screen.CREATE_PIN, Screen.CHANGE_PIN -> {
                        val changing = screen == Screen.CHANGE_PIN
                        if (changing) BackHandler { screen = Screen.SETTINGS }
                        PinSetupScreen(onDone = { pin ->
                            lifecycleScope.launch {
                                app.credentials.setPin(pin)
                                screen = when {
                                    changing -> Screen.SETTINGS
                                    Biometrics.isAvailable(this@MainActivity) -> Screen.BIOMETRIC
                                    else -> finishOnboarding()
                                }
                            }
                        })
                    }
                    Screen.BIOMETRIC -> BiometricOfferScreen(
                        onUse = {
                            Biometrics.enroll(this) { ok ->
                                lifecycleScope.launch {
                                    app.settings.setBiometricEnabled(ok)
                                    screen = finishOnboarding()
                                }
                            }
                        },
                        onSkip = { lifecycleScope.launch { screen = finishOnboarding() } },
                    )
                    Screen.UNLOCK -> UnlockGateScreen(
                        biometricEnabled = settings.biometricEnabled,
                        onUnlocked = { screen = Screen.HOME },
                    )
                    Screen.HOME -> {
                        val health = remember(resumeTick, running, settings.protectedPackages) {
                            SystemChecks.health(this, running, settings.protectedPackages.size)
                        }
                        HomeScreen(
                            health = health,
                            lastLock = lastLock,
                            lastLockLabel = lastLock?.let { app.catalog.label(it.packageName) },
                            onEnableService = { openSystem { SystemChecks.openAccessibilitySettings(this) } },
                            onOpenAppDetails = { openSystem { SystemChecks.openAppDetails(this) } },
                            onFixBattery = { openSystem { SystemChecks.openBatterySettings(this) } },
                            onApps = { screen = Screen.APPS },
                            onSettings = { screen = Screen.SETTINGS },
                        )
                    }
                    Screen.APPS -> {
                        BackHandler { screen = Screen.HOME }
                        AppsScreen(
                            catalog = app.catalog,
                            protectedPackages = settings.protectedPackages,
                            onToggle = { pkg, on -> lifecycleScope.launch { app.settings.setProtected(pkg, on) } },
                            onBack = { screen = Screen.HOME },
                        )
                    }
                    Screen.SETTINGS -> {
                        BackHandler { screen = Screen.HOME }
                        SettingsScreen(
                            settings = settings,
                            biometricAvailable = remember(resumeTick) { Biometrics.isAvailable(this) },
                            version = BuildConfig.VERSION_NAME,
                            onRelock = { policy -> lifecycleScope.launch { app.settings.setRelock(policy) } },
                            onBiometric = { on ->
                                if (on) {
                                    Biometrics.enroll(this) { ok -> lifecycleScope.launch { app.settings.setBiometricEnabled(ok) } }
                                } else {
                                    lifecycleScope.launch { app.settings.setBiometricEnabled(false) }
                                }
                            },
                            onChangePin = { screen = Screen.CHANGE_PIN },
                            onBack = { screen = Screen.HOME },
                        )
                    }
                }
            }
        }
    }

    private suspend fun finishOnboarding(): Screen {
        container.settings.setOnboardingDone()
        return Screen.HOME
    }

    private fun openSystem(action: () -> Unit) {
        skipRelockOnce = true
        action()
    }

    override fun onResume() {
        super.onResume()
        resumeTick++
    }

    override fun onStop() {
        super.onStop()
        // O próprio Vedra também é protegido (RF-09).
        if (!skipRelockOnce && screen in setOf(Screen.HOME, Screen.APPS, Screen.SETTINGS, Screen.CHANGE_PIN)) {
            screen = Screen.UNLOCK
        }
        skipRelockOnce = false
    }
}
