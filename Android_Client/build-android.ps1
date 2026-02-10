<#
Build script for Android client (React Native / Gradle / Android Studio style).
Usage:
    .\build-android.ps1 -Configuration Release

This script will try to run Gradle wrapper if present under Android_Client/android/gradlew.
If not found, it will try `./gradlew` at repository root.
#>
param(
    [string]$Configuration = "Release"
)

Set-StrictMode -Version Latest
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Push-Location $ScriptDir

Write-Host "Starting Android client build (Configuration=$Configuration)..."

$gradleWrapper = Join-Path $ScriptDir "android\gradlew"
$rootGradle = Join-Path (Resolve-Path "..").Path "gradlew"

if (Test-Path $gradleWrapper) {
    Write-Host "Using gradle wrapper at $gradleWrapper"
    & "$gradleWrapper" -p android assemble$Configuration
} elseif (Test-Path $rootGradle) {
    Write-Host "Using root gradle wrapper at $rootGradle"
    & "$rootGradle" -p android assemble$Configuration
} else {
    Write-Warning "No gradle wrapper found. Make sure Android project is present."
}

Pop-Location
Write-Host "Android build script finished."
