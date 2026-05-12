# Local Runbook

## Goal

Run the first minimal chain:

`EMQX -> Spring Boot backend -> MySQL`

and let `mock-device` continuously publish telemetry to:

`iot/demo-001/telemetry`

## 1. Start infrastructure

From [docker-compose.yml](E:\work\IoT-project\code\docker\docker-compose.yml):

```powershell
cd E:\work\IoT-project\code\docker
docker compose up -d
```

Expected:

- EMQX MQTT port: `1883`
- EMQX dashboard: `http://localhost:18083`
- MySQL port: `3306`

If the current machine cannot pull `EMQX` from Docker Hub, use the local fallback compose instead:

```powershell
cd E:\work\IoT-project\code\docker
docker compose -f docker-compose.local.yml up -d
```

Notes:

- this fallback uses `Mosquitto` only as a local `Broker`
- the backend access / telemetry chain remains unchanged
- switch back to `EMQX` when the image source is available

## 2. Start backend

```powershell
cd E:\work\IoT-project\code\backend
mvn -q "-Dmaven.repo.local=E:/work/IoT-project/m2repo" -pl iot-platform-boot -am spring-boot:run
```

Expected:

- Flyway creates `telemetry_event`
- backend subscribes to `iot/+/telemetry`

## 3. Start mock device

```powershell
cd E:\work\IoT-project\code\mock-device
mvn -q "-Dmaven.repo.local=E:/work/IoT-project/m2repo" exec:java
```

Optional one-shot verification:

```powershell
cd E:\work\IoT-project\code\mock-device
mvn -q "-Dmaven.repo.local=E:/work/IoT-project/m2repo" exec:java "-Dmock.maxMessages=3"
```

Useful overrides:

- `-Dmock.deviceId=demo-002`
- `-Dmock.topic=iot/demo-002/telemetry`
- `-Dmock.intervalMs=2000`
- `-Dmock.maxMessages=10`

## 4. Verify data

```powershell
docker exec iot_mysql mysql -uroot -proot iot_alarm_copilot -e "SELECT id, device_id, temperature, humidity, reported_at, created_at FROM telemetry_event ORDER BY id DESC LIMIT 10;"
```

## 5. Expected result

- mock-device logs show published MQTT messages
- backend logs show telemetry ingestion
- MySQL `telemetry_event` contains inserted rows
