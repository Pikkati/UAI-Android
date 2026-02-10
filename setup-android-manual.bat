@echo off
REM UAI Android Development Environment Setup (Windows Batch)
REM This script installs the basic Android development tools using standard Windows methods

echo.
echo ========================================
echo UAI Android Development Environment Setup
echo ========================================
echo.

REM Check if running as administrator
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Running as Administrator: YES
) else (
    echo WARNING: Not running as Administrator. Some installations may fail.
    echo Consider running this script as Administrator.
    echo.
)

REM Create Android SDK directory
echo Setting up Android SDK directory...
set ANDROID_SDK_PATH=%LOCALAPPDATA%\Android\Sdk
if not exist "%ANDROID_SDK_PATH%" (
    mkdir "%ANDROID_SDK_PATH%"
    echo Created: %ANDROID_SDK_PATH%
)

REM Set environment variables (current session)
set ANDROID_HOME=%ANDROID_SDK_PATH%
set ANDROID_SDK_ROOT=%ANDROID_SDK_PATH%

echo.
echo Environment variables set:
echo ANDROID_HOME = %ANDROID_HOME%
echo ANDROID_SDK_ROOT = %ANDROID_SDK_ROOT%

REM Set environment variables permanently (requires admin)
echo.
echo Setting permanent environment variables...
setx ANDROID_HOME "%ANDROID_SDK_PATH%" >nul 2>&1
setx ANDROID_SDK_ROOT "%ANDROID_SDK_PATH%" >nul 2>&1

REM Update PATH
echo Updating PATH...
set NEW_PATH=%ANDROID_SDK_PATH%\platform-tools;%ANDROID_SDK_PATH%\tools;%ANDROID_SDK_PATH%\tools\bin;%ANDROID_SDK_PATH%\emulator
setx PATH "%PATH%;%NEW_PATH%" >nul 2>&1

echo.
echo ========================================
echo Manual Installation Instructions
echo ========================================
echo.
echo Please complete the following steps manually:
echo.
echo 1. JAVA JDK 17:
echo    - Download from: https://adoptium.net/
echo    - Install the Windows x64 .msi installer
echo    - Set JAVA_HOME environment variable
echo.
echo 2. ANDROID STUDIO:
echo    - Download from: https://developer.android.com/studio
echo    - Install with default settings
echo    - Run Android Studio and install SDK components
echo.
echo 3. NODE.JS (if not already installed):
echo    - Download from: https://nodejs.org/
echo    - Install LTS version (18.x or 20.x)
echo.
echo 4. REACT NATIVE CLI:
echo    - Open Command Prompt and run: npm install -g @react-native-community/cli
echo.
echo 5. ANDROID SDK COMPONENTS (in Android Studio):
echo    - Open Android Studio
echo    - Go to Tools ^> SDK Manager
echo    - Install Android 34 (API Level 34)
echo    - Install Android SDK Build-Tools 34.0.0
echo    - Install Android Emulator
echo    - Install Intel x86 Emulator Accelerator (HAXM)
echo.
echo 6. CREATE ANDROID VIRTUAL DEVICE:
echo    - In Android Studio, go to Tools ^> AVD Manager
echo    - Click "Create Virtual Device"
echo    - Select Pixel 7 device
echo    - Download and select Android 34 system image
echo    - Name it "UAI_Router_Emulator"
echo.
echo ========================================
echo Quick Verification Commands
echo ========================================
echo.
echo After manual installation, run these commands to verify:
echo.
echo java -version
echo node --version
echo npm --version
echo %%ANDROID_HOME%%\platform-tools\adb.exe version
echo %%ANDROID_HOME%%\emulator\emulator.exe -list-avds
echo.
echo ========================================
echo Next Steps
echo ========================================
echo.
echo 1. Complete the manual installations above
echo 2. Restart your terminal/command prompt
echo 3. Navigate to: Android_App\UAIRouter
echo 4. Run: npm install
echo 5. Use build script: build-android-app.ps1
echo.

pause
