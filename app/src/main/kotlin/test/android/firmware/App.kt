package test.android.firmware

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import test.android.firmware.provider.Admins
import test.android.firmware.provider.Contexts
import test.android.firmware.provider.FinalAdmins
import test.android.firmware.provider.Providers

internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val contexts = Contexts(
            main = Dispatchers.Main,
            default = Dispatchers.Default,
        )
        val job = SupervisorJob()
        val coroutineScope = CoroutineScope(contexts.main + job)
        val context: Context = this
        val admins: Admins = FinalAdmins(
            context = context,
            coroutineScope = coroutineScope,
            default = contexts.default,
        )
        _providers = Providers(
            contexts = contexts,
            admins = admins,
        )
    }

    companion object {
        private var _providers: Providers? = null
        val providers: Providers get() = checkNotNull(_providers) { "No providers!" }
    }
}
