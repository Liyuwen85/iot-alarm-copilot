# backend

后端按 `DDD + 模块化单体 + Spring Cloud Alibaba Ready` 组织。

## 当前模块

- `iot-platform-boot` 单体应用启动入口
- `iot-integration-contract` 共享协作契约模块，统一承载跨上下文事件契约，以及像 `TelemetryMetricName`、`TelemetryMetrics` 这类多个上下文复用的共享语义对象
- `iot-persistence-support` 数据库公共支撑，只负责数据源、事务、Flyway、MyBatis 扫描等横切基础设施。
- `iot-context-access` 接入上下文，职责“设备怎么进入平台”，如：topic解析、接入身份验证、幂等键、协议到领域命令事件的转换等；MQTT 接入适配实现在 `access.interfaces.mqtt`。
- `iot-context-device` 设备上下文，负责设备注册、身份、影子、分组、生命周期、产品模型这类“设备主数据”。
- `iot-context-telemetry` 遥测上下文，负责原始遥测接入后的schema校验、标准化、聚合、最新状态等。对应的是“设备发来的数据如何变成平台统一指标语义”。
- `iot-context-rule` 规则上下文，负责规则定义、条件判断、命中结果。像阈值规则、组合规则、事件联动等都在这里。
- `iot-context-alarm` 告警上下文，负责告警生成、告警状态流转、告警查询等。规则命中后，应该进入此上下文中处理。
- `iot-context-inspection` 巡检上下文，负责基于告警生成巡检建议、工单预留、处理确认闭环。后面可承接“AI建议”的业务执行。
- `iot-context-ai` AI上下文，负责Prompt版本、摘要生成契约、结构化输出schema等；外部模型网关实现在 `ai.infrastructure.gateway`。
- `iot-context-audit` 审计上下文，负责关键事件留痕、操作审计、回访链路等。
