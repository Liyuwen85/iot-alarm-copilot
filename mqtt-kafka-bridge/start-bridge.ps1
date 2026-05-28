param(
    [string]$NodeId = "node1",
    [string]$JavaCmd = "java",
    [string]$MqttBrokerUrl = "tcp://localhost:1883",
    [string]$MqttTopicFilter = "iot/+/telemetry,iot/device/+/command-acks",
    [int]$MqttQos = 1,
    [string]$KafkaBootstrapServers = "localhost:9092",
    [string]$TelemetryKafkaTopic = "iot.telemetry.raw",
    [string]$CommandAckKafkaTopic = "iot.command.ack.raw",
    [bool]$LeaderElectionEnabled = $true,
    [string]$LeaderJdbcUrl = "jdbc:postgresql://localhost:5432/iot_alarm_copilot",
    [string]$LeaderJdbcUsername = "postgres",
    [string]$LeaderJdbcPassword = "postgres",
    [long]$LeaderLockKey = 20260518001,
    [long]$LeaderRetryIntervalMs = 3000,
    [long]$LeaderHealthCheckIntervalMs = 5000
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$jarPath = Join-Path $scriptDir "target\mqtt-kafka-bridge-0.1.0-SNAPSHOT-all.jar"

if (-not (Test-Path $jarPath)) {
    throw "Bridge jar not found: $jarPath . Run 'mvn -DskipTests package' first."
}

$env:BRIDGE_MQTT_CLIENT_ID = "mqtt-kafka-bridge-$NodeId"
$env:BRIDGE_MQTT_BROKER_URL = $MqttBrokerUrl
$env:BRIDGE_MQTT_TOPIC_FILTER = $MqttTopicFilter
$env:BRIDGE_MQTT_QOS = $MqttQos.ToString()
$env:BRIDGE_KAFKA_BOOTSTRAP_SERVERS = $KafkaBootstrapServers
$env:BRIDGE_TELEMETRY_KAFKA_TOPIC = $TelemetryKafkaTopic
$env:BRIDGE_COMMAND_ACK_KAFKA_TOPIC = $CommandAckKafkaTopic
$env:BRIDGE_LEADER_ELECTION_ENABLED = $LeaderElectionEnabled.ToString().ToLowerInvariant()
$env:BRIDGE_LEADER_JDBC_URL = $LeaderJdbcUrl
$env:BRIDGE_LEADER_JDBC_USERNAME = $LeaderJdbcUsername
$env:BRIDGE_LEADER_JDBC_PASSWORD = $LeaderJdbcPassword
$env:BRIDGE_LEADER_LOCK_KEY = $LeaderLockKey.ToString()
$env:BRIDGE_LEADER_RETRY_INTERVAL_MS = $LeaderRetryIntervalMs.ToString()
$env:BRIDGE_LEADER_HEALTH_CHECK_INTERVAL_MS = $LeaderHealthCheckIntervalMs.ToString()

Write-Host "starting mqtt-kafka-bridge node=$NodeId jar=$jarPath"
Write-Host "mqttBroker=$MqttBrokerUrl kafkaBootstrap=$KafkaBootstrapServers telemetryKafkaTopic=$TelemetryKafkaTopic commandAckKafkaTopic=$CommandAckKafkaTopic leaderLockKey=$LeaderLockKey"

& $JavaCmd "-jar" $jarPath
