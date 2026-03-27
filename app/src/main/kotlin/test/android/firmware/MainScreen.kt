package test.android.firmware

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import test.android.firmware.provider.Admins
import java.io.File

@Composable
internal fun MainScreen() {
    val providers = remember { App.providers }
    val isDeviceOwner = providers.admins.owners.collectAsState().value
    val devices = remember { mutableStateOf<Admins.DeviceInfo?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val logger = remember { providers.loggers.create("[Main]") }
    LaunchedEffect(Unit) {
        withContext(providers.contexts.default) {
            devices.value = providers.admins.getDeviceInfo()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        ) {
            val deviceInfo = devices.value
            if (deviceInfo != null) {
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(16.dp),
                    text = "serial number: ${deviceInfo.serialNumber}",
                )
            }
            BasicText(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(16.dp),
                text = "owner: $isDeviceOwner",
            )
            if (isDeviceOwner) {
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable {
                            providers.admins.update(isDeviceOwner = false)
                        }
                        .wrapContentSize(),
                    text = "remove admin",
                )
            }
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable {
                        providers.admins.test()
                    }
                    .wrapContentSize(),
                text = "test",
            )
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable {
                        coroutineScope.launch {
                            withContext(providers.contexts.default) {
                                val parent = Environment.getExternalStorageDirectory()
//                                val parent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                                val file = File(parent, "fs6-fw-20260327.zip")
//                                val file = File(parent, "fs6-fw-unknown.zip")
//                                val file = File(parent, "foo.zip")
                                val file = File(parent, "update.zip")
                                if (!file.exists()) TODO()
                                if (!file.isFile) TODO()
                                logger.debug("file: ${file.absolutePath}")
                                providers.admins.ota(file = file)
                            }
                        }
                    }
                    .wrapContentSize(),
                text = "ota",
            )
        }
    }
}
