#!/usr/bin/env powershell
<#
.SYNOPSIS
    UAI Android Development Environment Setup Script

.DESCRIPTION
    This script installs and configures all requirements for building Android applications
    including Java JDK, Android SDK, Android Studio, React Native CLI, and emulators.

.PARAMETER Force
    Force reinstallation of components even if they exist

.PARAMETER SkipAndroidStudio
    Skip Android Studio installation (useful for CI/CD environments)

.PARAMETER CreateEmulator
    Create and configure an Android emulator after installation

.EXAMPLE
    .\setup-android-environment.ps1

.EXAMPLE
    .\setup-android-environment.ps1 -Force -CreateEmulator

.NOTES
    Author: UAI Systems
    Version: 1.0.0
    Requires: PowerShell 5.1+ and Administrator privileges
#>

[CmdletBinding()]
param(
    [switch]$Force = $false,
    [switch]$SkipAndroidStudio = $false,
    [switch]$CreateEmulator = $true
)

# Requires Administrator privileges
if (-NOT ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Error "This script requires Administrator privileges. Please run as Administrator."
    exit 1
}

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# Configuration
$Config = @{
    AndroidSDKPath = "$env:LOCALAPPDATA\Android\Sdk"
    AndroidStudioPath = "$env:LOCALAPPDATA\JetBrains\Toolbox\apps\AndroidStudio"
    JavaVersion = "17"
    NodeVersion = "18.18.0"
    AvdName = "UAI_Router_Emulator"
    AvdDevice = "pixel_7"
    AvdSystemImage = "system-images;android-34;google_apis;x86_64"
    RequiredPackages = @(
        "platform-tools",
        "platforms;android-34",
        "platforms;android-33",
        "platforms;android-32",
        "build-tools;34.0.0",
        "build-tools;33.0.2",
        "system-images;android-34;google_apis;x86_64",
        "system-images;android-33;google_apis;x86_64",
        "emulator",
        "tools"
    )
}

function Write-Progress-Step {
    param([string]$Message, [string]$Status = "Processing")
    Write-Host "🔄 $Message" -ForegroundColor Cyan
    Write-Progress -Activity "Android Environment Setup" -Status $Status -CurrentOperation $Message
}

function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Warning-Custom {
    param([string]$Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
}

function Test-Command {
    param([string]$Command)
    try {
        Get-Command $Command -ErrorAction Stop | Out-Null
        return $true
    }
    catch {
        return $false
    }
}

function Install-Chocolatey {
    if (Test-Command "choco") {
        Write-Success "Chocolatey is already installed"
        return
    }

    Write-Progress-Step "Installing Chocolatey package manager"
    Set-ExecutionPolicy Bypass -Scope Process -Force
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
    iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

    # Refresh environment
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
    Write-Success "Chocolatey installed successfully"
}

function Install-JavaJDK {
    Write-Progress-Step "Checking Java JDK installation"

    if (-not $Force) {
        try {
            $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_.ToString() }
            if ($javaVersion -and $javaVersion -match "17\.") {
                Write-Success "Java JDK 17 is already installed: $javaVersion"
                return
            }
        }
        catch {
            # Java not found, continue with installation
        }
    }

    Write-Progress-Step "Installing Java JDK $($Config.JavaVersion)"
    choco install openjdk$($Config.JavaVersion) -y

    # Set JAVA_HOME
    $javaHome = "${env:ProgramFiles}\Eclipse Adoptium\jdk-$($Config.JavaVersion)*"
    $javaHomeResolved = Get-ChildItem $javaHome | Select-Object -First 1 -ExpandProperty FullName
    if ($javaHomeResolved) {
        [Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHomeResolved, "Machine")
        $env:JAVA_HOME = $javaHomeResolved
        Write-Success "JAVA_HOME set to: $javaHomeResolved"
    }

    Write-Success "Java JDK $($Config.JavaVersion) installed successfully"
}

function Install-NodeJS {
    Write-Progress-Step "Checking Node.js installation"

    if (-not $Force -and (Test-Command "node")) {
        $nodeVersion = node --version
        Write-Success "Node.js is already installed: $nodeVersion"
        return
    }

    Write-Progress-Step "Installing Node.js $($Config.NodeVersion)"
    choco install nodejs --version=$($Config.NodeVersion) -y

    # Refresh environment
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
    Write-Success "Node.js $($Config.NodeVersion) installed successfully"
}

function Install-AndroidStudio {
    if ($SkipAndroidStudio) {
        Write-Warning-Custom "Skipping Android Studio installation as requested"
        return
    }

    Write-Progress-Step "Checking Android Studio installation"

    if (-not $Force -and (Test-Path $Config.AndroidStudioPath)) {
        Write-Success "Android Studio is already installed"
        return
    }

    Write-Progress-Step "Installing Android Studio"
    choco install androidstudio -y
    Write-Success "Android Studio installed successfully"
}

function Install-AndroidSDK {
    Write-Progress-Step "Setting up Android SDK"

    # Set Android SDK path
    if (-not (Test-Path $Config.AndroidSDKPath)) {
        New-Item -Path $Config.AndroidSDKPath -ItemType Directory -Force | Out-Null
    }

    [Environment]::SetEnvironmentVariable("ANDROID_HOME", $Config.AndroidSDKPath, "Machine")
    [Environment]::SetEnvironmentVariable("ANDROID_SDK_ROOT", $Config.AndroidSDKPath, "Machine")
    $env:ANDROID_HOME = $Config.AndroidSDKPath
    $env:ANDROID_SDK_ROOT = $Config.AndroidSDKPath

    # Add Android tools to PATH
    $androidTools = @(
        "$($Config.AndroidSDKPath)\platform-tools",
        "$($Config.AndroidSDKPath)\tools",
        "$($Config.AndroidSDKPath)\tools\bin",
        "$($Config.AndroidSDKPath)\emulator"
    )

    $currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
    foreach ($tool in $androidTools) {
        if ($currentPath -notlike "*$tool*") {
            $currentPath = "$currentPath;$tool"
        }
    }
    [Environment]::SetEnvironmentVariable("Path", $currentPath, "Machine")
    $env:Path = $currentPath

    Write-Success "Android SDK environment configured"
}

function Install-AndroidSDKPackages {
    Write-Progress-Step "Installing Android SDK packages"

    # Download command line tools if not present
    $cmdlineToolsPath = "$($Config.AndroidSDKPath)\cmdline-tools"
    if (-not (Test-Path $cmdlineToolsPath)) {
        Write-Progress-Step "Downloading Android command line tools"
        $toolsUrl = "https://dl.google.com/android/repository/commandlinetools-win-9477386_latest.zip"
        $toolsZip = "$env:TEMP\cmdline-tools.zip"

        Invoke-WebRequest -Uri $toolsUrl -OutFile $toolsZip
        Expand-Archive -Path $toolsZip -DestinationPath $cmdlineToolsPath -Force

        # Move to correct structure
        $extractedPath = "$cmdlineToolsPath\cmdline-tools"
        $latestPath = "$cmdlineToolsPath\latest"
        if (Test-Path $extractedPath) {
            Move-Item $extractedPath $latestPath -Force
        }

        Remove-Item $toolsZip -Force
        Write-Success "Android command line tools downloaded"
    }

    $sdkmanager = "$cmdlineToolsPath\latest\bin\sdkmanager.bat"

    if (Test-Path $sdkmanager) {
        Write-Progress-Step "Installing Android SDK packages"

        # Accept licenses
        Write-Progress-Step "Accepting Android SDK licenses"
        echo "y" | & $sdkmanager "--licenses" | Out-Null

        # Install required packages
        foreach ($package in $Config.RequiredPackages) {
            Write-Progress-Step "Installing $package"
            & $sdkmanager "$package" | Out-Null
        }

        Write-Success "Android SDK packages installed successfully"
    }
    else {
        Write-Warning-Custom "SDK Manager not found. Manual installation may be required."
    }
}

function Install-ReactNative {
    Write-Progress-Step "Installing React Native CLI"

    if (-not $Force -and (Test-Command "npx")) {
        try {
            $rnVersion = npx react-native --version 2>$null
            if ($rnVersion) {
                Write-Success "React Native CLI is already available"
                return
            }
        }
        catch {
            # Continue with installation
        }
    }

    npm install -g @react-native-community/cli
    Write-Success "React Native CLI installed successfully"
}

function Create-AndroidEmulator {
    if (-not $CreateEmulator) {
        Write-Warning-Custom "Skipping emulator creation as requested"
        return
    }

    Write-Progress-Step "Creating Android emulator: $($Config.AvdName)"

    $avdmanager = "$($Config.AndroidSDKPath)\cmdline-tools\latest\bin\avdmanager.bat"

    if (-not (Test-Path $avdmanager)) {
        Write-Warning-Custom "AVD Manager not found. Cannot create emulator."
        return
    }

    # Check if emulator already exists
    $existingAvds = & $avdmanager list avd | Select-String "Name: $($Config.AvdName)"
    if ($existingAvds -and -not $Force) {
        Write-Success "Emulator '$($Config.AvdName)' already exists"
        return
    }

    # Create emulator
    $createArgs = @(
        "create", "avd",
        "--name", $Config.AvdName,
        "--package", $Config.AvdSystemImage,
        "--device", $Config.AvdDevice
    )

    Write-Progress-Step "Creating AVD with system image: $($Config.AvdSystemImage)"
    echo "no" | & $avdmanager @createArgs | Out-Null

    Write-Success "Android emulator '$($Config.AvdName)' created successfully"
}

function Install-RequiredGlobalPackages {
    Write-Progress-Step "Installing required global packages"

    $packages = @(
        "yarn",
        "expo-cli",
        "react-native-cli"
    )

    foreach ($package in $packages) {
        Write-Progress-Step "Installing $package"
        npm install -g $package | Out-Null
    }

    Write-Success "Global packages installed successfully"
}

function Test-AndroidEnvironment {
    Write-Progress-Step "Testing Android development environment"

    $tests = @()

    # Test Java
    try {
        $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_.ToString() }
        $tests += @{ Name = "Java JDK"; Status = "✅ $javaVersion"; Success = $true }
    }
    catch {
        $tests += @{ Name = "Java JDK"; Status = "❌ Not found"; Success = $false }
    }

    # Test Node.js
    try {
        $nodeVersion = node --version
        $tests += @{ Name = "Node.js"; Status = "✅ $nodeVersion"; Success = $true }
    }
    catch {
        $tests += @{ Name = "Node.js"; Status = "❌ Not found"; Success = $false }
    }

    # Test Android SDK
    if ($env:ANDROID_HOME -and (Test-Path $env:ANDROID_HOME)) {
        $tests += @{ Name = "Android SDK"; Status = "✅ $env:ANDROID_HOME"; Success = $true }
    }
    else {
        $tests += @{ Name = "Android SDK"; Status = "❌ Not configured"; Success = $false }
    }

    # Test ADB
    try {
        $adbPath = "$env:ANDROID_HOME\platform-tools\adb.exe"
        if (Test-Path $adbPath) {
            $tests += @{ Name = "ADB"; Status = "✅ Available"; Success = $true }
        }
        else {
            $tests += @{ Name = "ADB"; Status = "❌ Not found"; Success = $false }
        }
    }
    catch {
        $tests += @{ Name = "ADB"; Status = "❌ Error"; Success = $false }
    }

    # Test Emulator
    try {
        $emulatorPath = "$env:ANDROID_HOME\emulator\emulator.exe"
        if (Test-Path $emulatorPath) {
            $tests += @{ Name = "Emulator"; Status = "✅ Available"; Success = $true }
        }
        else {
            $tests += @{ Name = "Emulator"; Status = "❌ Not found"; Success = $false }
        }
    }
    catch {
        $tests += @{ Name = "Emulator"; Status = "❌ Error"; Success = $false }
    }

    # Display results
    Write-Host "`n📋 Environment Test Results:" -ForegroundColor Cyan
    Write-Host "================================" -ForegroundColor Cyan

    $allSuccess = $true
    foreach ($test in $tests) {
        Write-Host "$($test.Name): $($test.Status)"
        if (-not $test.Success) {
            $allSuccess = $false
        }
    }

    Write-Host "================================" -ForegroundColor Cyan

    if ($allSuccess) {
        Write-Success "All components installed and configured successfully!"
        return $true
    }
    else {
        Write-Warning-Custom "Some components failed. Please check the installation."
        return $false
    }
}

function Show-NextSteps {
    Write-Host "`n🚀 Next Steps:" -ForegroundColor Green
    Write-Host "===============" -ForegroundColor Green
    Write-Host "1. Restart your terminal/IDE to pick up environment changes"
    Write-Host "2. Navigate to your React Native project directory"
    Write-Host "3. Run 'npm install' or 'yarn install' to install dependencies"
    Write-Host "4. Use the build script: .\build-android-app.ps1"
    Write-Host "5. Start the emulator: .\start-emulator.ps1"
    Write-Host ""
    Write-Host "📱 Emulator created: $($Config.AvdName)" -ForegroundColor Cyan
    Write-Host "🔧 Android SDK: $($Config.AndroidSDKPath)" -ForegroundColor Cyan
    Write-Host "☕ Java Home: $env:JAVA_HOME" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Happy coding! 🎉" -ForegroundColor Green
}

# Main execution
try {
    Write-Host "🚀 UAI Android Development Environment Setup" -ForegroundColor Green
    Write-Host "=============================================" -ForegroundColor Green
    Write-Host ""

    Install-Chocolatey
    Install-JavaJDK
    Install-NodeJS
    Install-AndroidStudio
    Install-AndroidSDK
    Install-AndroidSDKPackages
    Install-ReactNative
    Install-RequiredGlobalPackages
    Create-AndroidEmulator

    Write-Host ""
    $success = Test-AndroidEnvironment

    if ($success) {
        Show-NextSteps
    }

    Write-Host ""
    Write-Success "Android development environment setup completed!"
}
catch {
    Write-Error "Setup failed: $($_.Exception.Message)"
    Write-Host "Please check the error above and try again." -ForegroundColor Red
    exit 1
}
finally {
    Write-Progress -Activity "Android Environment Setup" -Completed
}
