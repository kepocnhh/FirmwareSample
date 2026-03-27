# FirmwareSample
Sample application for updating firmware on Android.

---

### Set device owner

```
$ adb shell dpm set-device-owner test.android.firmware.debug/test.android.firmware.MainDeviceAdminReceiver
```

#### List owners

```
$ adb shell dpm list-owners
```

#### Unset device owner

```
$ adb shell dpm remove-active-admin test.android.firmware.debug/test.android.firmware.MainDeviceAdminReceiver
```

#### Force stop

```
$ adb shell am force-stop test.android.firmware.debug
```

---
