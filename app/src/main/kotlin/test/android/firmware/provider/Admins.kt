package test.android.firmware.provider

import kotlinx.coroutines.flow.StateFlow
import java.io.File

internal interface Admins {
    data class DeviceInfo(
        val serialNumber: String,
    )

    val owners: StateFlow<Boolean>

    fun update(isDeviceOwner: Boolean)
    fun getDeviceInfo(): DeviceInfo
    fun test()
    fun ota(file: File)
}
