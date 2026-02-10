Param(
  [string]$AvdName = "UAI_Router_Emulator"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Wait-For-Device {
  param([string]$Serial = '')
  $adb = "$env:ANDROID_HOME\platform-tools\adb.exe"
  & $adb start-server | Out-Null
  for($i=0;$i -lt 120;$i++){
    Start-Sleep -Seconds 2
    # If a serial is provided, target that device explicitly to avoid errors when multiple devices are present
    if(-not [string]::IsNullOrEmpty($Serial)) {
      $state = (& $adb -s $Serial get-state 2>$null)
      if($state -match 'device'){
        $bc = (& $adb -s $Serial shell getprop sys.boot_completed 2>$null).Trim()
        if($bc -eq '1'){ return }
      }
    } else {
      # Prefer emulator devices (emulator-*) to avoid physical unauthorized devices interfering
      $deviceList = (& $adb devices) -split "\r?\n"
      $emu = $null
      foreach($line in $deviceList) {
        if($line -match '^(emulator-\d+)\s+device') { $emu = $Matches[1]; break }
      }
      if($emu) {
        $state = (& $adb -s $emu get-state 2>$null)
        if($state -match 'device'){
          $bc = (& $adb -s $emu shell getprop sys.boot_completed 2>$null).Trim()
          if($bc -eq '1'){ return }
        }
      }
    }
  }
  throw 'Device did not become ready in time.'
}

# Resolve paths
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$androidDir = Join-Path $root 'android'
Push-Location $androidDir

try {
  # SDK path
  if (-not $env:ANDROID_HOME) { $env:ANDROID_HOME = "C:\Users\Administrator\AppData\Local\Android\Sdk" }
  $emulator = Join-Path $env:ANDROID_HOME 'emulator\emulator.exe'
  $adb = Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe'

  # Build
  Write-Host '==> Building debug APK'
  $buildLog = Join-Path $androidDir 'android-build.log'
  .\gradlew.bat clean assembleDebug *>&1 | Tee-Object -FilePath $buildLog

  $apk = Resolve-Path '.\app\build\outputs\apk\debug\app-debug.apk'
  Write-Host "==> APK: $apk"

  # Boot emulator if needed
  & $adb start-server | Out-Null
  $devices = & $adb devices | Select-String 'emulator-'
  if(-not $devices){
    Write-Host "==> Starting emulator: $AvdName"
    Start-Process -FilePath $emulator -ArgumentList @('-avd', $AvdName, '-no-snapshot', '-accel','on','-gpu','angle_indirect','-no-boot-anim') -WindowStyle Normal | Out-Null
  } else {
    Write-Host '==> Emulator already running'
  }

  # Wait for device
  Wait-For-Device
  $match = & $adb devices -l | Select-String -Pattern '^\s*(emulator-\d+)\b' | Select-Object -First 1
  if($match -and $match.Matches.Count -gt 0){
    $serial = $match.Matches[0].Groups[1].Value.Trim()
  }
  if(-not $serial){ throw 'No emulator serial found after boot.' }
  Write-Host "==> Using device: $serial"

  # Install and launch
  Write-Host '==> Installing APK'
  $installLog = Join-Path $androidDir 'android-install.log'
  & $adb -s $serial install -r "$apk" *>&1 | Tee-Object -FilePath $installLog
  Write-Host '==> Clearing app data'
  & $adb -s $serial shell pm clear com.uairouter | Out-Null
  Write-Host '==> Launching MainActivity'
  & $adb -s $serial shell am start -W -n com.uairouter/.MainActivity

  # Stay awake
  Write-Host '==> Keeping emulator awake'
  & $adb -s $serial shell settings put global stay_on_while_plugged_in 3
  & $adb -s $serial shell svc power stayon true

  Write-Host '==> Done. App should be visible on the emulator.' -ForegroundColor Green
}
finally {
  Pop-Location
}
