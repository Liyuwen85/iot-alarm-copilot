param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$Limit = 20
)

function Invoke-DemoQuery {
    param(
        [string]$Name,
        [string]$Path
    )

    $uri = "$BaseUrl$Path?limit=$Limit"
    Write-Host "== $Name =="
    $result = Invoke-RestMethod -Uri $uri -Method Get
    $result | ConvertTo-Json -Depth 8
    Write-Host ""
    return @($result).Count
}

$telemetryCount = Invoke-DemoQuery -Name "telemetry" -Path "/api/telemetry-events/recent"
$alarmCount = Invoke-DemoQuery -Name "alarm" -Path "/api/alarms/recent"
$auditCount = Invoke-DemoQuery -Name "audit" -Path "/api/audit-logs/recent"

[pscustomobject]@{
    telemetryCount = $telemetryCount
    alarmCount = $alarmCount
    auditCount = $auditCount
} | ConvertTo-Json
