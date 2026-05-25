param(
    [string]$DeviceId = "demo-001",
    [string]$BrokerUrl = "tcp://localhost:1883",
    [int]$Qos = 1,
    [int]$IntervalMs = 2000,
    [int]$MaxMessages = 10,
    [string]$MavenRepo = ""
)

$mockDeviceRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\mock-device")).Path

$arguments = @(
    "-Dmock.deviceId=$DeviceId",
    "-Dmock.brokerUrl=$BrokerUrl",
    "-Dmock.qos=$Qos",
    "-Dmock.intervalMs=$IntervalMs",
    "-Dmock.maxMessages=$MaxMessages",
    "exec:java"
)

Push-Location $mockDeviceRoot
try {
    & mvn @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "mock-device failed to run"
    }
} finally {
    Pop-Location
}
