package br.com.auradevcode.vedra

import android.app.Application
import android.content.Context
import br.com.auradevcode.vedra.core.LockEngine
import br.com.auradevcode.vedra.guard.GuardHost
import br.com.auradevcode.vedra.ui.lock.LockActivity

class VedraApp : Application(), GuardHost {

    lateinit var container: AppContainer
        private set

    override val lockEngine: LockEngine get() = container.lockEngine

    override val secureWindows: Boolean = !BuildConfig.DEBUG

    override fun launchLock(context: Context, packageName: String) = LockActivity.start(context, packageName)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this).also { it.start() }
    }
}

val Context.container: AppContainer get() = (applicationContext as VedraApp).container
