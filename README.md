# STPI Screen Privacy 0.1.0

## What this build does
- Read-only Root shell diagnostics for MediaProjection, display virtual-device records, and PROJECT_MEDIA AppOps.
- Android helper app targeting API 37.
- The helper can request the Android system picker with app-window source enabled and whole-display source disabled.
- The helper does not capture, record, stream, or forward screen pixels.

## Critical scope boundary
The API 37 picker configuration applies only to a MediaProjection session requested by STPI Screen Privacy itself. It does NOT impose a system-wide policy on Xuexitong (`com.chaoxing.mobile`) or another third-party app. An ordinary app cannot reliably force another app's capture request to use app-window mode. If Xuexitong's own system picker offers "Entire screen", cancel and do not proceed; choose an individual app window if offered.

Android's app screen sharing is documented from Android 14 (API 34). Android 15 QPR1+ adds a prominent system status-bar chip for active projection sessions on supported builds. OEM behavior may differ.

## Shell installation (phone)
1. Copy `shell/STPI_Screen_Privacy.sh` to `/sdcard/STPI_Screen_Privacy.sh`.
2. In a root terminal:
   ```sh
   su
   chmod 755 /sdcard/STPI_Screen_Privacy.sh
   sh /sdcard/STPI_Screen_Privacy.sh check
   ```
3. Monitor every 5 seconds:
   ```sh
   sh /sdcard/STPI_Screen_Privacy.sh monitor 5
   ```
4. Stop monitor with Ctrl+C. Log is written to `/sdcard/STPI_Screen_Privacy/privacy_monitor.log`.

The shell script is read-only. It does not kill apps, change AppOps, change permissions, alter SELinux, or modify system properties.

## Build APK
Requires Android Studio/Gradle with Android SDK Platform 37 and JDK 17:
```sh
cd android
gradle assembleDebug
```
APK output:
`android/app/build/outputs/apk/debug/app-debug.apk`

The current package contains Android source, not a compiled APK. This execution environment does not have Android SDK Platform 37 / Gradle installed, so an APK binary was not fabricated.
