param(
    [string]$MavenRepo = ""
)

$backendRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\backend")).Path
$bootModuleRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\backend\iot-platform-boot")).Path

$installArguments = @(
    "-pl", "iot-platform-boot",
    "-am",
    "-DskipTests",
    "install"
)

$runArguments = @(
    "spring-boot:run"
)

function Test-TcpPort {
    param(
        [string]$Host,
        [int]$Port,
        [int]$TimeoutMs = 2000
    )

    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $asyncResult = $client.BeginConnect($Host, $Port, $null, $null)
        if (-not $asyncResult.AsyncWaitHandle.WaitOne($TimeoutMs, $false)) {
            return $false
        }
        $client.EndConnect($asyncResult)
        return $true
    } catch {
        return $false
    } finally {
        $client.Close()
    }
}

function Ensure-BackendDatastoresReady {
    if (-not (Test-TcpPort -Host "localhost" -Port 5432)) {
        throw "PostgreSQL/TimescaleDB is not reachable on localhost:5432. Start the local stack first with .\scripts\start-local-mosquitto.ps1 -Action up."
    }

    $dockerCommand = Get-Command docker -ErrorAction SilentlyContinue
    if ($null -eq $dockerCommand) {
        return
    }

    $runningState = ""
    try {
        $runningState = (& docker inspect -f "{{.State.Running}}" iot_timescaledb 2>$null | Select-Object -First 1)
    } catch {
        return
    }

    if ("$runningState".Trim() -eq "true") {
        $ready = $false
        for ($i = 0; $i -lt 30; $i++) {
            & docker exec iot_timescaledb pg_isready -U postgres -d postgres *> $null
            if ($LASTEXITCODE -eq 0) {
                $ready = $true
                break
            }
            Start-Sleep -Seconds 1
        }

        if (-not $ready) {
            throw "postgres in container 'iot_timescaledb' was not ready in time"
        }

        $databases = @("iot_alarm_copilot", "iot_telemetry_hot")
        foreach ($databaseName in $databases) {
            $exists = & docker exec iot_timescaledb psql -U postgres -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '$databaseName';"
            if ($LASTEXITCODE -ne 0) {
                throw "failed to query database list in container 'iot_timescaledb'"
            }
            if ("$exists".Trim() -ne "1") {
                & docker exec iot_timescaledb psql -U postgres -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE $databaseName;"
                if ($LASTEXITCODE -ne 0) {
                    throw "failed to create database '$databaseName' in container 'iot_timescaledb'"
                }
            }
        }
    }
}

if (-not [string]::IsNullOrWhiteSpace($MavenRepo)) {
    $installArguments = @("-Dmaven.repo.local=$MavenRepo") + $installArguments
    $runArguments = @("-Dmaven.repo.local=$MavenRepo") + $runArguments
}

Ensure-BackendDatastoresReady

Push-Location $backendRoot
try {
    & mvn @installArguments
    if ($LASTEXITCODE -ne 0) {
        throw "backend install failed"
    }
} finally {
    Pop-Location
}

Push-Location $bootModuleRoot
try {
    & mvn @runArguments
    if ($LASTEXITCODE -ne 0) {
        throw "backend failed to start"
    }
} finally {
    Pop-Location
}
