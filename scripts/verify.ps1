# One-command health check for the repository (Windows PowerShell).
#   1. Appium module: offline checks (APK sanity, locator contract, config) - needs only JDK 17+ and Maven.
#   2. Android app: unit tests + debug APK build - needs the Android SDK (ANDROID_HOME or local.properties).
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

Write-Host '==> Appium test module (offline checks; device tests self-skip without an Appium server)'
Push-Location (Join-Path $root 'appium-tests')
try {
    mvn -B -q test
    if ($LASTEXITCODE -ne 0) { throw 'Appium module tests failed' }
} finally { Pop-Location }
Write-Host '    OK'

if ($env:ANDROID_HOME -or $env:ANDROID_SDK_ROOT -or (Test-Path (Join-Path $root 'local.properties'))) {
    Write-Host '==> Android app: unit tests and debug build'
    Push-Location $root
    try {
        & .\gradlew.bat --console=plain :app:testDebugUnitTest :app:assembleDebug
        if ($LASTEXITCODE -ne 0) { throw 'Gradle build failed' }
    } finally { Pop-Location }
    Write-Host '    APK: app\build\outputs\apk\debug\app-debug.apk'
} else {
    Write-Host '==> Android app: SKIPPED (no Android SDK found)'
    Write-Host '    Install Android Studio or the SDK command-line tools, then set ANDROID_HOME and re-run.'
}
