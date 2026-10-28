# Usage: .\run-tests.ps1         -> offline checks + e2e tests (e2e self-skip without Appium)
#        .\run-tests.ps1 -E2E    -> only the device tests; requires a running Appium server
param([switch]$E2E)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$appiumUrl = if ($env:APPIUM_URL) { $env:APPIUM_URL } else { 'http://127.0.0.1:4723' }

if ($E2E) {
    try {
        Invoke-WebRequest -Uri "$appiumUrl/status" -TimeoutSec 3 -UseBasicParsing | Out-Null
    } catch {
        Write-Host "Appium server not reachable at $appiumUrl"
        Write-Host '  npm install -g appium; appium driver install uiautomator2; appium'
        Write-Host '  then start an Android emulator (adb devices should list it)'
        exit 1
    }
    mvn -q test -Dgroups=e2e "-Dappium.url=$appiumUrl"
} else {
    mvn -q test
}
exit $LASTEXITCODE
