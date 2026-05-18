param(
    [ValidateSet("up", "down", "restart", "logs")]
    [string]$Action = "up"
)

$composeFile = Join-Path $PSScriptRoot "..\\docker\\docker-compose.local.yml"

function Ensure-LocalPostgresDatabases {
    $containerName = "iot_timescaledb"
    $username = "postgres"

    $ready = $false
    for ($i = 0; $i -lt 30; $i++) {
        & docker exec $containerName pg_isready -U $username -d postgres *> $null
        if ($LASTEXITCODE -eq 0) {
            $ready = $true
            break
        }
        Start-Sleep -Seconds 1
    }

    if (-not $ready) {
        throw "postgres in container '$containerName' was not ready in time"
    }

    function Ensure-Database {
        param(
            [string]$DatabaseName
        )

        $exists = & docker exec $containerName psql -U $username -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '$DatabaseName';"
        if ($LASTEXITCODE -ne 0) {
            throw "failed to query database list in container '$containerName'"
        }

        if ("$exists".Trim() -ne "1") {
            & docker exec $containerName psql -U $username -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE $DatabaseName;"
            if ($LASTEXITCODE -ne 0) {
                throw "failed to create database '$DatabaseName' in container '$containerName'"
            }
        }
    }

    Ensure-Database -DatabaseName "iot_alarm_copilot"
    Ensure-Database -DatabaseName "iot_telemetry_hot"
}

switch ($Action) {
    "up" {
        docker compose -f $composeFile up -d
        if ($LASTEXITCODE -ne 0) {
            throw "docker compose up failed"
        }
        Ensure-LocalPostgresDatabases
    }
    "down" {
        docker compose -f $composeFile down
    }
    "restart" {
        docker compose -f $composeFile down
        if ($LASTEXITCODE -ne 0) {
            throw "docker compose down failed"
        }
        docker compose -f $composeFile up -d
        if ($LASTEXITCODE -ne 0) {
            throw "docker compose up failed"
        }
        Ensure-LocalPostgresDatabases
    }
    "logs" {
        docker compose -f $composeFile logs -f
    }
}
