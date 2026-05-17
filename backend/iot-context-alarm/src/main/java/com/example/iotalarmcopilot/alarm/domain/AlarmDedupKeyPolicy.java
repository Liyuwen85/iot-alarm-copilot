package com.example.iotalarmcopilot.alarm.domain;

/**
 * 告警防止重复消费策略
 */
public final class AlarmDedupKeyPolicy {

    private AlarmDedupKeyPolicy() {
    }

    /**
     * 返回唯一键
     *
     * @param ruleCode
     * @param deviceId
     * @param telemetryEventId
     * @return
     */
    public static AlarmDedupKey build(String ruleCode, String deviceId, Long telemetryEventId) {
        return new AlarmDedupKey(ruleCode + ":" + deviceId + ":" + telemetryEventId);
    }
}
