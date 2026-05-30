package com.example.iotalarmcopilot.mockdevice.application;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 调度型防抖
 */
public class GatewayTelemetryPublishScheduler implements AutoCloseable {

    private final long debounceWindowMs;
    private final ScheduledExecutorService scheduledExecutor;
    private final Map<String, ScheduledFuture<?>> pendingTasks;

    public GatewayTelemetryPublishScheduler(long debounceWindowMs) {
        this.debounceWindowMs = debounceWindowMs;
        this.pendingTasks = new ConcurrentHashMap<>();
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "gateway-telemetry-publisher");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void schedule(String deviceId, Runnable task) {
        cancel(deviceId);

        ScheduledFuture<?> future = scheduledExecutor.schedule(() -> {
            try {
                task.run();
            } finally {
                pendingTasks.remove(deviceId);
            }
        }, debounceWindowMs, TimeUnit.MILLISECONDS);

        pendingTasks.put(deviceId, future);
    }

    public void cancel(String deviceId) {
        ScheduledFuture<?> future = pendingTasks.remove(deviceId);
        if (future != null) {
            future.cancel(false);
        }
    }

    @Override
    public void close() throws Exception {
        pendingTasks.values().forEach(future -> future.cancel(false));
        pendingTasks.clear();
        scheduledExecutor.shutdown();
    }
}
