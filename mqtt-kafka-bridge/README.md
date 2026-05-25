# mqtt-kafka-bridge

## Quick Start

1.  Build fat jar:
    

```powershell
mvn -DskipTests package
```

2.  Start one instance:
    

```powershell
.\start-bridge.ps1 -NodeId node1
```

3.  Start standby instance:
    

```powershell
.\start-bridge.ps1 -NodeId node2
```

这是一个很薄的 `MQTT -> Kafka` 桥接程序，用来在 `Mosquitto` 不直接提供 Kafka 上游能力时，把设备上行遥测转发到 Kafka。

* * *

## 1\. 当前链路

当前上行主链：

```text
Device / MQTTX
  -> Mosquitto (broker)
  -> mqtt-kafka-bridge
  -> Kafka topic: iot.telemetry.raw
  -> backend(access -> telemetry -> rule -> alarm ...)
```

## 2\. MQTT topic 约定

当前 bridge 假设设备上行 topic 结构为：

```text
iot/{deviceId}/telemetry
```

例如：

```text
iot/dev-01/telemetry
iot/boiler-sensor-02/telemetry
```

bridge 会从 topic 中提取出：

-   `deviceId （做为message key）`
    

并把原始 MQTT topic 和 payload 一起封装进 Kafka value。

当前封装对象为：

-   `KafkaTelemetryEnvelope`
    

其结构是：

```json
{
  "topic": "iot/dev-01/telemetry",
  "payload": "{\"temperature\":36.5,\"humidity\":55}"
}
```

## 3\. 配置项

当前配置来自：

-   JVM System Property
    
-   或环境变量
    

主要配置项：

-   `bridge.mqttBrokerUrl` / `BRIDGE_MQTT_BROKER_URL`
    
-   `bridge.mqttClientId` / `BRIDGE_MQTT_CLIENT_ID`
    
-   `bridge.mqttTopicFilter` / `BRIDGE_MQTT_TOPIC_FILTER`
    
-   `bridge.mqttQos` / `BRIDGE_MQTT_QOS`
    
-   `bridge.kafkaBootstrapServers` / `BRIDGE_KAFKA_BOOTSTRAP_SERVERS`
    
-   `bridge.kafkaTopic` / `BRIDGE_KAFKA_TOPIC`
    
-   `bridge.leaderElectionEnabled` / `BRIDGE_LEADER_ELECTION_ENABLED`
    
-   `bridge.leaderJdbcUrl` / `BRIDGE_LEADER_JDBC_URL`
    
-   `bridge.leaderJdbcUsername` / `BRIDGE_LEADER_JDBC_USERNAME`
    
-   `bridge.leaderJdbcPassword` / `BRIDGE_LEADER_JDBC_PASSWORD`
    
-   `bridge.leaderLockKey` / `BRIDGE_LEADER_LOCK_KEY`
    
-   `bridge.leaderRetryIntervalMs` / `BRIDGE_LEADER_RETRY_INTERVAL_MS`
    
-   `bridge.leaderHealthCheckIntervalMs` / `BRIDGE_LEADER_HEALTH_CHECK_INTERVAL_MS`
    

默认值：

-   MQTT Broker：`tcp://localhost:1883`
    
-   MQTT clientId：`mqtt-kafka-bridge`
    
-   MQTT topic filter：`iot/+/telemetry`
    
-   Kafka bootstrap：`localhost:9092`
    
-   Kafka topic：`iot.telemetry.raw`
    
-   Leader election：`true`
    
-   Leader JDBC：`jdbc:postgresql://localhost:5432/iot_alarm_copilot`
    
-   Leader lock key：`20260518001`
    

* * *

## 4\. 多节点部署方式（主要为了演示）

当前实现采用的是：

`多实例部署 + PostgreSQL advisory lock 单活抢占`

## 5\. 生产建议

**直接使用EMQX**。

如果要把这个 bridge 用在更接近生产的环境，至少要注意下面几点：

1.  Kafka topic 要配置足够的 partition 数，否则 backend 多 node 也无法真正并行。
    
2.  设备 topic 规则要保持稳定，确保 `deviceId` 可被准确提取。
    
3.  不要随意把 Kafka key 改成随机值、时间戳或完整 payload。
    
4.  如果后面换成 EMQX 等本身支持 Kafka 集成的 Broker，可以用 Broker 原生能力替换这个 bridge，但仍要保留“按设备 key 分区”原则。
    
5.  如果启用多实例 bridge，务必给每个实例不同的 `mqttClientId`，即使当前是单活模式，也不要复用同一个固定 clientId。