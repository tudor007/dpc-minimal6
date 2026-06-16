# SimpleDPC — GMS-Free Device Policy Controller

A minimal Android DPC (Device Policy Controller) for AOSP/GMS-free devices
such as the **Uniwa F55** and similar rugged handsets running Android 12.

## Features
- Screen lock
- Password minimum length policy
- Screen timeout enforcement
- Camera disable/enable
- Factory reset block (Device Owner)
- USB file transfer block (Device Owner)
- Factory wipe (with double confirmation)
- Boot receiver to maintain policies across reboots
- **Zero Google Play Services dependencies**

---

## Build Requirements

| Tool | Version |
|------|---------|
| Android Studio | Hedgehog 2023.1+ (or any with Gradle 8.x) |
| JDK | 11 or 17 |
| Android SDK | API 31 (Android 12) |
| Gradle | 8.2 (auto-downloaded by wrapper) |

### Build from command line

```bash
# On macOS/Linux
./gradlew assembleDebug

# On Windows
gradlew.bat assembleDebug
```

Output APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## Deployment to Device

### Step 1 — Install APK

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Step 2 — Set Device Owner

The device must have **no Google accounts** added. On a fresh/factory-reset
device (or a GMS-free device like Uniwa):

```bash
adb shell dpm set-device-owner com.simpledpc/.AdminReceiver
```

Expected response:
```
Success: Device owner set to package com.simpledpc
```

### Step 3 — Open the app

All policy controls will now be unlocked.

---

## Troubleshooting

| Error | Fix |
|-------|-----|
| `Not allowed to set the device owner because there are already some accounts on the device` | Remove all Google accounts from Settings first |
| `java.lang.IllegalStateException: Not allowed to set...` | Factory reset device, install APK before adding any account |
| Controls greyed out | App is not set as Device Owner — repeat Step 2 |

---

## Architecture

```
AdminReceiver   ← DeviceAdminReceiver (registered in manifest)
    │
    └─► PolicyManager  ← All DevicePolicyManager calls (GMS-free)
            │
            ├─► MainActivity  (UI)
            └─► BootReceiver  (policy refresh on reboot)
```

---

## Extending the DPC

Add calls in `PolicyManager.java`:

```java
// Disable screenshots (Device Owner)
dpm.setScreenCaptureDisabled(adminComponent, true);

// Lock to single app (kiosk mode)
dpm.setLockTaskPackages(adminComponent, new String[]{"com.myapp"});
startLockTask();

// Install a certificate
dpm.installCaCert(adminComponent, certBytes);
```

See: https://developer.android.com/reference/android/app/admin/DevicePolicyManager
