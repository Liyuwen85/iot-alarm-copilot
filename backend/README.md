# backend

后端按 `DDD + 模块化单体 + Spring Cloud Alibaba Ready` 组织。

## 当前模块

- `iot-platform-boot`
- `iot-shared-kernel`
- `iot-context-access`
- `iot-context-device`
- `iot-context-telemetry`
- `iot-context-rule`
- `iot-context-alarm`
- `iot-context-inspection`
- `iot-context-ai`
- `iot-context-audit`
- `iot-adapter-mqtt`
- `iot-adapter-persistence`
- `iot-adapter-ai`

## 设计原则

- 先单进程运行
- 先定上下文边界
- 上下文之间优先通过 `application service` 和 `domain event` 协作
- 不跨上下文直接共用 mapper
- `shared-kernel` 保持很薄

后续预留模块：

- `iot-context-notification`
- `iot-context-query`
- `iot-context-telemetry-processing`
- `iot-context-edge-node`

