#!/usr/bin/env powershell
<#
.SYNOPSIS
    UAI Android App Build Script

.DESCRIPTION
    Automated build script for the UAI Router Android application.
    This script handles dependency installation, building, and deployment.

.PARAMETER BuildType
    Build type: debug, release, or bundle

.PARAMETER StartEmulator
    Start Android emulator if not running

.PARAMETER InstallApp
    Install the built APK to connected device/emulator

.PARAMETER LaunchApp
    Launch the app after installation

.PARAMETER Clean
    Clean build directory before building

.EXAMPLE
    .\build-android-app.ps1

.EXAMPLE
    .\build-android-app.ps1 -BuildType release -StartEmulator -InstallApp -LaunchApp

.NOTES
    Author: UAI Systems
    Version: 1.0.0
    Requires: Android development environment (use setup-android-environment.ps1)
#>

[CmdletBinding()]
param(
    [ValidateSet("debug", "release", "bundle")]
    [string]$BuildType = "debug",

    [switch]$StartEmulator = $false,
    [switch]$InstallApp = $false,
    [switch]$LaunchApp = $false,
    [switch]$Clean = $false
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# Configuration
$Config = @{
    ProjectRoot = "$PSScriptRoot\Android_App\UAIRouter"
    AndroidProjectRoot = "$PSScriptRoot\Android_App\UAIRouter\android"
    AppPackage = "com.uairouter"
    MainActivity = "com.uairouter.MainActivity"
    AvdName = "UAI_Router_Emulator"
    BuildTimeout = 600  # 10 minutes
}

function Write-Progress-Step {
    param([string]$Message, [string]$Status = "Processing")
    Write-Host "🔄 $Message" -ForegroundColor Cyan
    Write-Progress -Activity "UAI Android App Build" -Status $Status -CurrentOperation $Message
}

function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Warning-Custom {
    param([string]$Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
}

function Test-Prerequisites {
    Write-Progress-Step "Checking prerequisites"

    $missing = @()

    # Check Android environment
    if (-not $env:ANDROID_HOME) {
        $missing += "ANDROID_HOME environment variable"
    }

    if (-not (Test-Path "$env:ANDROID_HOME\platform-tools\adb.exe")) {
        $missing += "Android ADB"
    }

    if (-not (Get-Command "node" -ErrorAction SilentlyContinue)) {
        $missing += "Node.js"
    }

    if (-not (Get-Command "java" -ErrorAction SilentlyContinue)) {
        $missing += "Java JDK"
    }

    if ($missing.Count -gt 0) {
        Write-Error "Missing prerequisites: $($missing -join ', '). Please run setup-android-environment.ps1 first."
        return $false
    }

    Write-Success "All prerequisites met"
    return $true
}

function Test-ProjectStructure {
    Write-Progress-Step "Validating project structure"

    if (-not (Test-Path $Config.ProjectRoot)) {
        Write-Error "Project root not found: $($Config.ProjectRoot)"
        return $false
    }

    if (-not (Test-Path "$($Config.ProjectRoot)\package.json")) {
        Write-Error "package.json not found in project root"
        return $false
    }

    if (-not (Test-Path $Config.AndroidProjectRoot)) {
        Write-Error "Android project directory not found: $($Config.AndroidProjectRoot)"
        return $false
    }

    if (-not (Test-Path "$($Config.AndroidProjectRoot)\gradlew.bat")) {
        Write-Error "Gradle wrapper not found in Android project"
        return $false
    }

    Write-Success "Project structure validated"
    return $true
}

function Install-Dependencies {
    Write-Progress-Step "Installing Node.js dependencies"

    Push-Location $Config.ProjectRoot
    try {
        # Install npm dependencies
        if (Test-Path "yarn.lock") {
            Write-Progress-Step "Installing dependencies with Yarn"
            yarn install --frozen-lockfile
        }
        else {
            Write-Progress-Step "Installing dependencies with npm"
            npm install
        }

        Write-Success "Dependencies installed successfully"
    }
    finally {
        Pop-Location
    }
}

function Start-AndroidEmulator {
    if (-not $StartEmulator) {
        return
    }

    Write-Progress-Step "Starting Android emulator"

    $emulator = "$env:ANDROID_HOME\emulator\emulator.exe"
    $adb = "$env:ANDROID_HOME\platform-tools\adb.exe"

    # Check if emulator is already running
    & $adb start-server | Out-Null
    $devices = & $adb devices | Select-String 'emulator-'

    if ($devices) {
        Write-Success "Emulator is already running"
        return
    }

    # Start emulator
    Write-Progress-Step "Launching emulator: $($Config.AvdName)"
    $emulatorArgs = @(
        '-avd', $Config.AvdName,
        '-no-snapshot',
        '-accel', 'on',
        '-gpu', 'angle_indirect',
        '-no-boot-anim',
        '-verbose'
    )

    Start-Process -FilePath $emulator -ArgumentList $emulatorArgs -WindowStyle Normal

    # Wait for emulator to boot
    Write-Progress-Step "Waiting for emulator to boot (may take 2-3 minutes)"
    $timeout = 180  # 3 minutes
    $elapsed = 0

    while ($elapsed -lt $timeout) {
        Start-Sleep -Seconds 5
        $elapsed += 5

        $devices = & $adb devices | Select-String 'emulator-.*device'
        if ($devices) {
            # Check if boot is completed
            $serial = ($devices[0] -split '\s+')[0]
            $bootCompleted = & $adb -s $serial shell getprop sys.boot_completed 2>$null

            if ($bootCompleted -eq "1") {
                Write-Success "Emulator booted successfully: $serial"
                return
            }
        }

        Write-Progress-Step "Waiting for emulator... ($elapsed/$timeout seconds)"
    }

    Write-Warning-Custom "Emulator boot timeout. Continuing anyway..."
}

function Build-AndroidApp {
    Write-Progress-Step "Building Android app ($BuildType)"

    Push-Location $Config.AndroidProjectRoot
    try {
        # Clean if requested
        if ($Clean) {
            Write-Progress-Step "Cleaning build directory"
            .\gradlew.bat clean
        }

        # Build based on type
        switch ($BuildType) {
            "debug" {
                Write-Progress-Step "Building debug APK"
                .\gradlew.bat assembleDebug
                $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
            }
            "release" {
                Write-Progress-Step "Building release APK"
                .\gradlew.bat assembleRelease
                $apkPath = "app\build\outputs\apk\release\app-release.apk"
            }
            "bundle" {
                Write-Progress-Step "Building Android App Bundle"
                .\gradlew.bat bundleRelease
                $apkPath = "app\build\outputs\bundle\release\app-release.aab"
            }
        }

        # Verify build output
        $fullApkPath = Join-Path $Config.AndroidProjectRoot $apkPath
        if (Test-Path $fullApkPath) {
            $apkSize = [math]::Round((Get-Item $fullApkPath).Length / 1MB, 2)
            Write-Success "Build completed successfully: $fullApkPath ($apkSize MB)"
            return $fullApkPath
        }
        else {
            throw "Build output not found: $fullApkPath"
        }
    }
    finally {
        Pop-Location
    }
}

function Install-AndroidApp {
    param([string]$ApkPath)

    if (-not $InstallApp) {
        return
    }

    Write-Progress-Step "Installing Android app"

    $adb = "$env:ANDROID_HOME\platform-tools\adb.exe"

    # Get connected devices
    $devices = & $adb devices | Select-String -Pattern '^\s*([^\s]+)\s+device\s*$'

    if (-not $devices) {
        Write-Warning-Custom "No Android devices found. Skipping installation."
        return
    }

    $deviceSerial = $devices[0].Matches[0].Groups[1].Value
    Write-Progress-Step "Installing to device: $deviceSerial"

    # Uninstall existing app
    & $adb -s $deviceSerial uninstall $Config.AppPackage 2>$null | Out-Null

    # Install new app
    & $adb -s $deviceSerial install -r "$ApkPath"

    if ($LASTEXITCODE -eq 0) {
        Write-Success "App installed successfully"

        if ($LaunchApp) {
            Write-Progress-Step "Launching app"
            & $adb -s $deviceSerial shell am start -W -n "$($Config.MainActivity)"
            Write-Success "App launched successfully"
        }
    }
    else {
        Write-Warning-Custom "App installation failed"
    }
}

function Show-BuildSummary {
    param([string]$ApkPath, [TimeSpan]$BuildTime)

    Write-Host "`n📱 Build Summary" -ForegroundColor Green
    Write-Host "=================" -ForegroundColor Green
    Write-Host "Build Type: $BuildType"
    Write-Host "Output: $ApkPath"

    if (Test-Path $ApkPath) {
        $apkSize = [math]::Round((Get-Item $ApkPath).Length / 1MB, 2)
        Write-Host "Size: $apkSize MB"
    }

    Write-Host "Build Time: $($BuildTime.ToString('mm\:ss'))"
    Write-Host ""

    if ($InstallApp) {
        Write-Host "✅ App installed on device" -ForegroundColor Green
    }

    if ($LaunchApp) {
        Write-Host "🚀 App launched successfully" -ForegroundColor Green
    }

    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "- Test the app on your device/emulator"
    Write-Host "- Check logs with: adb logcat"
    Write-Host "- Monitor performance with Android Studio"
}

# Main execution
try {
    $startTime = Get-Date

    Write-Host "🚀 UAI Android App Build Process" -ForegroundColor Green
    Write-Host "===================================" -ForegroundColor Green
    Write-Host "Build Type: $BuildType"
    Write-Host "Project: $($Config.ProjectRoot)"
    Write-Host ""

    # Validate environment and project
    if (-not (Test-Prerequisites)) { exit 1 }
    if (-not (Test-ProjectStructure)) { exit 1 }

    # Install dependencies
    Install-Dependencies

    # Start emulator if requested
    Start-AndroidEmulator

    # Build the app
    $apkPath = Build-AndroidApp

    # Install and launch if requested
    Install-AndroidApp -ApkPath $apkPath

    # Show summary
    $buildTime = (Get-Date) - $startTime
    Show-BuildSummary -ApkPath $apkPath -BuildTime $buildTime

    Write-Success "Android app build completed successfully!"
}
catch {
    Write-Error "Build failed: $($_.Exception.Message)"
    Write-Host "Check the error above and try again." -ForegroundColor Red
    exit 1
}
finally {
    Write-Progress -Activity "UAI Android App Build" -Completed
}
