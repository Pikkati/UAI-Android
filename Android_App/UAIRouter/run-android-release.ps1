Param(
  [string]$KeystorePath = "release.keystore",
  [string]$KeystoreAlias = "uai_release",
  [string]$KeystorePassword = "changeit"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$androidDir = Join-Path $root 'android'
Push-Location $androidDir

try {
  $buildLog = Join-Path $androidDir 'android-release-build.log'
  Write-Host '==> Building signed release APK'
  # Set up gradle properties for signing
  $gradleProps = @(
    "MYAPP_RELEASE_STORE_FILE=$KeystorePath",
    "MYAPP_RELEASE_KEY_ALIAS=$KeystoreAlias",
    "MYAPP_RELEASE_STORE_PASSWORD=$KeystorePassword",
    "MYAPP_RELEASE_KEY_PASSWORD=$KeystorePassword"
  )
  Set-Content -Path 'gradle.properties' -Value $gradleProps -Encoding UTF8

  .\gradlew.bat clean assembleRelease *>&1 | Tee-Object -FilePath $buildLog

  $apk = Resolve-Path '.\app\build\outputs\apk\release\app-release.apk'
  Write-Host "==> Release APK: $apk"

  # Optionally build AAB
  Write-Host '==> Building Android App Bundle (AAB)'
  .\gradlew.bat bundleRelease *>&1 | Tee-Object -FilePath (Join-Path $androidDir 'android-release-bundle.log')
  $aab = Resolve-Path '.\app\build\outputs\bundle\release\app-release.aab'
  Write-Host "==> Release AAB: $aab"

  Write-Host '==> Release build complete.' -ForegroundColor Green
}
finally {
  Pop-Location
}
