package test.android.firmware.provider

import android.app.admin.DevicePolicyManager
import android.content.Context
import com.sdkapi.api.SdkApi
import com.sdkapi.common.ApiTool
import com.sdkapi.sdkapiservice.ISdkCommonListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class FinalAdmins(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val default: CoroutineContext,
    loggers: Loggers,
) : Admins {
    private val logger = loggers.create("[Admins]")
    override val owners = object : StateFlow<Boolean> {
        override val value: Boolean
            get() {
                val dm = context.getSystemService(DevicePolicyManager::class.java)
                return dm.isDeviceOwnerApp(context.packageName)
            }
        override val replayCache = emptyList<Boolean>()

        override suspend fun collect(collector: FlowCollector<Boolean>): Nothing {
            val dm = context.getSystemService(DevicePolicyManager::class.java)
            val values = AtomicBoolean(dm.isDeviceOwnerApp(context.packageName))
            collector.emit(values.get())
            while (true) {
                val isDeviceOwner = dm.isDeviceOwnerApp(context.packageName)
                if (values.compareAndSet(!isDeviceOwner, isDeviceOwner)) {
                    collector.emit(isDeviceOwner)
                }
                delay(1.seconds)
            }
        }
    }

    private val listener : ISdkCommonListener = object : ISdkCommonListener.Stub() {
        override fun CommonListenerNotify(
            actionId: Int,
            value: String?,
            iArray: IntArray?,
            progress: Int,
        ) {
            logger.debug("action: $actionId")
            when (actionId) {
                ApiTool.CommonEvent.GET_SCREEN_SIGNAL_BRIGHTNESS -> {
                    logger.debug("iArray: ${iArray?.toList()}")
                    logger.debug("progress: $progress")
                    runCatching {
                        value!!.toInt()
                    }.fold(
                        onSuccess = {
                            logger.debug("brightness: $it")
                        },
                        onFailure = { error ->
                            logger.warning("get brightness error: $error")
                        },
                    )
                }
                ApiTool.CommonEvent.UPDATE_SYSTEM_OTA -> {
                    logger.debug("iArray: ${iArray?.toList()}")
                    logger.debug("progress: $progress")
                    logger.debug("value: $value")
                }
            }
        }
    }

    private val pid = android.os.Process.myPid()

    init {
        SdkApi.newInstance(context)
        coroutineScope.launch {
            withContext(default) {
                withTimeout(4.seconds) {
                    while (isActive) {
                        if (SdkApi.getInstance().serviceConnectionStatus) break
                        delay(250.milliseconds)
                    }
                }
                SdkApi.getInstance().CommonEvent().registerCommonListener(pid, listener)
            }
        }
    }

    override fun update(isDeviceOwner: Boolean) {
        val dm = context.getSystemService(DevicePolicyManager::class.java)
        if (dm.isDeviceOwnerApp(context.packageName) == isDeviceOwner) return
        if (isDeviceOwner) error("Set an app the device owner is not supported!")
        dm.clearDeviceOwnerApp(context.packageName)
    }

    override fun getDeviceInfo(): Admins.DeviceInfo {
        check(SdkApi.getInstance().serviceConnectionStatus)
        val di = SdkApi.getInstance().DeviceInfo()
        return Admins.DeviceInfo(
            serialNumber = di.serialNumber,
        )
    }

    override fun test() {
        val actionId = ApiTool.CommonEvent.GET_SCREEN_SIGNAL_BRIGHTNESS
        SdkApi.getInstance().CommonEvent().setCommonEvent(pid, actionId, "", intArrayOf(), 0)
    }

    override fun ota(file: File) {
        val actionId = ApiTool.CommonEvent.UPDATE_SYSTEM_OTA
        SdkApi.getInstance().CommonEvent().setCommonEvent(pid, actionId, file.absolutePath, intArrayOf(1), 1)
    }
}
