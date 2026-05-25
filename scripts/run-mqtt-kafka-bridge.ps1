param(
    [string]$MqttBrokerUrl = "tcp://localhost:1883",
    [string]$MqttClientId = "mqtt-kafka-bridge",
    [string]$MqttTopicFilter = "iot/+/telemetry",
    [int]$MqttQos = 1,
    [string]$KafkaBootstrapServers = "localhost:9092",
    [string]$KafkaTopic = "iot.telemetry.raw",
    [string]$MavenRepo = ""
)

$bridgeRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\mqtt-kafka-bridge")).Path

$arguments = @(
    "compile",
    "-Dbridge.mqttBrokerUrl=$MqttBrokerUrl",
    "-Dbridge.mqttClientId=$MqttClientId",
    "-Dbridge.mqttTopicFilter=$MqttTopicFilter",
    "-Dbridge.mqttQos=$MqttQos",
    "-Dbridge.kafkaBootstrapServers=$KafkaBootstrapServers",
    "-Dbridge.kafkaTopic=$KafkaTopic",
    "exec:java"
)

Push-Location $bridgeRoot
try {
    & mvn @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "mqtt-kafka-bridge failed to run"
    }
} finally {
    Pop-Location
}
