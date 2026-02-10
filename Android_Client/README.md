# Android Client — Build & Packaging

This folder contains helper scripts to build and package the Android client (React Native or native Android).

Files:
- `build-android.ps1` — Attempts to run the Gradle wrapper to assemble the specified build variant (default: Release).
- `assemble-android.ps1` — Finds the generated APK/AAB and copies it into `Android_Client/../artifacts`.

Quick start (PowerShell):

```powershell
# From repository root
cd Android_Client
.\build-android.ps1 -Configuration Release
.\assemble-android.ps1 -Configuration Release -OutputPath ..\artifacts
```

CI note:
- A GitHub Actions workflow is included to build on an Ubuntu runner using the Android SDK. Ensure required secrets (e.g., signing keys) are configured for release builds.

If your project uses a different build system (Flutter, Cordova), adapt the scripts accordingly.