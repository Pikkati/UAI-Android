<#
Build script for Android client (Kivy / Buildozer).
Usage:
    .\build-android.ps1 -Configuration Release

This script will use Buildozer to build the Kivy Android app.
#>
param(
    [string]$Configuration = "Release"
)

Set-StrictMode -Version Latest
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Push-Location $ScriptDir

Write-Host "Starting Android client build (Configuration=$Configuration)..."

# Check if buildozer is installed
if (-not (Get-Command buildozer -ErrorAction SilentlyContinue)) {
    Write-Host "Buildozer not found. Installing..."
    pip install buildozer
}

# Check if buildozer.spec exists
$specFile = Join-Path $ScriptDir "buildozer.spec"
if (-not (Test-Path $specFile)) {
    Write-Error "buildozer.spec not found in $ScriptDir"
    Pop-Location
    exit 1
}

# Build the Android APK
Write-Host "Building Android APK with Buildozer..."
if ($Configuration -eq "Release") {
    & buildozer android release
} else {
    & buildozer android debug
}

Pop-Location
Write-Host "Android build script finished."
