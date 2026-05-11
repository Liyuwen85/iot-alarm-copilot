# iot-alarm-copilot

这是一个样板工程，主要为了从0到1学习 IoT后端开发，跑通主核心lian'l。

当前阶段目标：

-   `环境监测 + 异常告警 + 巡检工单 + AI 摘要`
    
-   `Spring Boot 模块化单体`
    
-   `DDD 上下文边界`
    
-   `Spring Cloud Alibaba Ready`
    

## 当前骨架

```text
.
├─ backend/                  # Java 后端多模块骨架
├─ docker/                   # 本地依赖
├─ docs/                     # 设计文档与架构图
├─ mock-device/              # Java Mock Device 骨架
├─ scripts/                  # 启动与初始化脚本
└─ README.md
```

## 当前模块边界

-   `access`
    
-   `device`
    
-   `telemetry`
    
-   `rule`
    
-   `alarm`
    
-   `inspection`
    
-   `ai`
    
-   `audit`
    

后续预留：

-   `notification`
    
-   `query`
    
-   `telemetry-processing`
    
-   `edge-node`
    

## 当前状态

当前仓库先完成了：

-   设计文档
    
-   目录骨架
    
-   Maven 多模块骨架
    
-   Docker Compose
    
-   初始化 SQL
    
-   Mock Device 骨架
    

下一步按文档顺序继续做：

1.  起 `EMQX + MySQL`
    
2.  细化 `device / telemetry` 上下文
    
3.  接 MQTT 消费
    
4.  做规则与告警
    
5.  接 AI 摘要