package com.example.iotalarmcopilot.telemetry.application;

import com.example.iotalarmcopilot.telemetry.domain.TelemetryEvent;

import java.util.List;

/**
 * 遥测热点数据端口
 */
public interface TelemetryHotDataPort {

    /**
     * 添加遥测事件
     *
     * @param event
     */
    void append(TelemetryEvent event);

    /**
     * 最近的遥测事件
     *
     * @param limit
     * @return
     */
    List<TelemetryEventVO> recent(int limit);
}
