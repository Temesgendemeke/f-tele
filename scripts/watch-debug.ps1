param(
    [string]$DeviceSerial = "",
    [switch]$NoLaunch,
    [int]$PollMilliseconds = 500
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$gradleWrapper = Join-Path $projectRoot "gradlew.bat"
$packageName = "com.example.ftele"
$mainActivity = "$packageName/.ui.MainActivity"

if (-not (Test-Path $gradleWrapper)) {
    throw "Gradle wrapper was not found at $gradleWrapper."
}

function Invoke-Adb {
    param([string[]]$Arguments)

    if ([string]::IsNullOrWhiteSpace($DeviceSerial)) {
        & adb @Arguments
    } else {
        & adb -s $DeviceSerial @Arguments
    }

    if ($LASTEXITCODE -ne 0) {
        throw "adb failed with exit code $LASTEXITCODE."
    }
}

function Get-WatchedFiles {
    $roots = @(
        (Join-Path $projectRoot "app\src"),
        (Join-Path $projectRoot "app\build.gradle"),
        (Join-Path $projectRoot "build.gradle"),
        (Join-Path $projectRoot "settings.gradle"),
        (Join-Path $projectRoot "gradle.properties")
    )

    $files = @()
    foreach ($root in $roots) {
        if (Test-Path $root -PathType Container) {
            $files += Get-ChildItem -Path $root -Recurse -File |
                Where-Object { $_.Extension -in @(".kt", ".java", ".xml", ".gradle", ".properties", ".json") }
        } elseif (Test-Path $root -PathType Leaf) {
            $files += Get-Item $root
        }
    }

    return @($files | ForEach-Object {
        "{0}|{1}|{2}" -f $_.FullName, $_.Length, $_.LastWriteTimeUtc.Ticks
    })
}

function Install-AndLaunch {
    Write-Host "`n[$(Get-Date -Format "HH:mm:ss")] Building and installing debug APK..." -ForegroundColor Cyan
    Push-Location $projectRoot
    try {
        & $gradleWrapper installDebug
        if ($LASTEXITCODE -ne 0) {
            throw "Gradle installDebug failed with exit code $LASTEXITCODE."
        }
    } finally {
        Pop-Location
    }

    if (-not $NoLaunch) {
        Invoke-Adb @("shell", "am", "force-stop", $packageName)
        Invoke-Adb @("shell", "am", "start", "-n", $mainActivity)
    }

    Write-Host "[$(Get-Date -Format "HH:mm:ss")] Ready. Watching for source changes..." -ForegroundColor Green
}

if (-not $NoLaunch) {
    Invoke-Adb @("get-state") | Out-Null
}

Install-AndLaunch
$lastSnapshot = Get-WatchedFiles

Write-Host "Press Ctrl+C to stop." -ForegroundColor DarkGray
while ($true) {
    Start-Sleep -Milliseconds $PollMilliseconds
    $currentSnapshot = Get-WatchedFiles
    if (-not [System.Linq.Enumerable]::SequenceEqual($lastSnapshot, $currentSnapshot)) {
        $lastSnapshot = $currentSnapshot
        try {
            Install-AndLaunch
        } catch {
            Write-Host "[$(Get-Date -Format "HH:mm:ss")] Change build failed: $($_.Exception.Message)" -ForegroundColor Red
            Write-Host "Watching continues; save another change to retry." -ForegroundColor Yellow
        }
    }
}
