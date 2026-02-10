#!/usr/bin/env powershell
<#
.SYNOPSIS
    UAI Android Environment Verification Script

.DESCRIPTION
    Checks the current Android development environment setup and provides
    installation guidance for missing components.
#>

Set-StrictMode -Version Latest
$ErrorActionPreference = 'SilentlyContinue'

function Test-Component {
    param(
        [string]$Name,
        [string]$TestCommand,
        [string]$ExpectedOutput = "",
        [string]$Path = ""
    )

    $result = @{
        Name = $Name
        Installed = $false
        Version = "Not found"
        Path = $Path
    }

    try {
        if ($Path -and (Test-Path $Path)) {
            $result.Installed = $true
            $result.Version = "Available at $Path"
        }
        elseif ($TestCommand) {
            $output = Invoke-Expression $TestCommand 2>$null
            if ($output) {
                $result.Installed = $true
                $result.Version = $output | Select-Object -First 1
            }
        }
    }
    catch {
        # Component not available
    }

    return $result
}

function Show-InstallationGuide {
    Write-Host ''
    Write-Host 'Android Development Setup Guide' -ForegroundColor Green
    Write-Host '====================================' -ForegroundColor Green
    Write-Host ''

    Write-Host '1. Install Chocolatey (Package Manager):' -ForegroundColor Cyan
    Write-Host '   Run as Administrator:'
    Write-Host '   Set-ExecutionPolicy Bypass -Scope Process -Force'
    Write-Host "   iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))"
    Write-Host ''

    Write-Host '2. Install Java JDK 17:' -ForegroundColor Cyan
    Write-Host '   choco install openjdk17 -y'
    Write-Host ''

    Write-Host '3. Install Node.js:' -ForegroundColor Cyan
    Write-Host '   choco install nodejs --version=18.18.0 -y'
    Write-Host ''

    Write-Host '4. Install Android Studio:' -ForegroundColor Cyan
    Write-Host '   choco install androidstudio -y'
    Write-Host '   OR download from: https://developer.android.com/studio'
    Write-Host ''

    $androidSdkPath = "$env:LOCALAPPDATA\Android\Sdk"
    Write-Host '5. Configure Android SDK:' -ForegroundColor Cyan
    Write-Host "   - Set ANDROID_HOME to: $androidSdkPath"
    Write-Host '   - Add to PATH: %ANDROID_HOME%\platform-tools'
    Write-Host '   - Add to PATH: %ANDROID_HOME%\tools'
    Write-Host '   - Add to PATH: %ANDROID_HOME%\emulator'
    Write-Host ''

    Write-Host '6. Install React Native CLI:' -ForegroundColor Cyan
    Write-Host '   npm install -g @react-native-community/cli'
    Write-Host ''

    Write-Host '7. Create Android Virtual Device:' -ForegroundColor Cyan
    Write-Host '   - Open Android Studio'
    Write-Host '   - Go to Tools and then AVD Manager'
    Write-Host '   - Create Virtual Device using Pixel 7 and Android 34 API'
    Write-Host ''
}

# Check components
Write-Host "UAI Android Environment Checker" -ForegroundColor Green
Write-Host "===================================" -ForegroundColor Green
Write-Host ""

$components = @(
    (Test-Component "Chocolatey" "choco --version"),
    (Test-Component "Java JDK" "java -version"),
    (Test-Component "Node.js" "node --version"),
    (Test-Component "npm" "npm --version"),
    (Test-Component "Android Studio" "" "$env:LOCALAPPDATA\JetBrains"),
    (Test-Component "Android SDK" "" "$env:LOCALAPPDATA\Android\Sdk"),
    (Test-Component "ADB" "" "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"),
    (Test-Component "Android Emulator" "" "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe"),
    (Test-Component "React Native CLI" "npx react-native --version")
)

$installedCount = 0
foreach ($component in $components) {
    $status = if ($component.Installed) { "[OK]" } else { "[MISSING]" }
    Write-Host "$status $($component.Name): $($component.Version)"
    if ($component.Installed) { $installedCount++ }
}

Write-Host ""
Write-Host "Installation Status: $installedCount/$($components.Count) components installed" -ForegroundColor $(if ($installedCount -eq $components.Count) { "Green" } else { "Yellow" })

if ($installedCount -lt $components.Count) {
    Show-InstallationGuide
}
else {
    Write-Host ''
    Write-Host 'All components are installed! You are ready to build Android apps.' -ForegroundColor Green
    Write-Host ''
    Write-Host 'Next steps:' -ForegroundColor Cyan
    Write-Host '1. Navigate to Android_App\UAIRouter directory'
    Write-Host '2. Run: npm install'
    Write-Host '3. Use build script: .\build-android-app.ps1'
}

Write-Host ''
Write-Host 'Environment Variables:' -ForegroundColor Cyan
Write-Host "ANDROID_HOME: $env:ANDROID_HOME"
Write-Host "JAVA_HOME: $env:JAVA_HOME"
$pathHasAndroid = $env:PATH -like "*Android*"
Write-Host "PATH includes Android tools: $pathHasAndroid"
