package test.android.firmware

import android.app.Application
import android.content.Context
import test.android.firmware.provider.Admins
import test.android.firmware.provider.FinalAdmins
import test.android.firmware.provider.Providers

internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val context: Context = this
        val admins: Admins = FinalAdmins(
            context = context,
        )
        _providers = Providers(
            admins = admins,
        )
    }

    companion object {
        private var _providers: Providers? = null
        val providers: Providers get() = checkNotNull(_providers) { "No providers!" }
    }
}
