# 本地运行最小闭环

当前统一只保留一条本地运行主链：

`Mock Device -> Mosquitto -> MQTT Kafka Bridge -> Kafka -> Backend -> Telemetry -> PostgreSQL / TimescaleDB`

## 1. 启动基础依赖

在项目根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-local-mosquitto.ps1 -Action up
```

默认会启动：

- `Mosquitto: 1883`
- `Kafka: 9092`
- `PostgreSQL / TimescaleDB: 5432`

脚本会自动确保两个数据库存在：

- `iot_alarm_copilot`
- `iot_telemetry_hot`

## 2. 启动 Backend

在项目根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-backend-local.ps1
```

默认 HTTP 端口：

- `8080`

## 3. 启动 MQTT Kafka Bridge

在项目根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-mqtt-kafka-bridge.ps1
```

默认配置：

- `mqttBrokerUrl=tcp://localhost:1883`
- `mqttTopicFilter=iot/+/telemetry`
- `kafkaBootstrapServers=localhost:9092`
- `kafkaTopic=iot.telemetry.raw`

## 4. 启动 Mock Device

在项目根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-mock-device.ps1 -BrokerUrl tcp://localhost:1883 -DeviceId demo-001 -IntervalMs 1000 -MaxMessages 10
```

## 5. 验证结果

执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\check-demo.ps1
```

或者手动访问：

```text
http://localhost:8080/api/telemetry-events/recent?limit=20
http://localhost:8080/api/alarms/recent?limit=20
http://localhost:8080/api/audit-logs/recent?limit=20
```

## 6. 关闭环境

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-local-mosquitto.ps1 -Action down
```
