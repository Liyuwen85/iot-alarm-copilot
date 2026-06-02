#include "modbus/modbus_collector.h"

#include <errno.h>
#include <stdio.h>
#include <modbus/modbus-tcp.h>
#include <modbus/modbus.h>

typedef struct modbus_collector_runtime {
    modbus_t *ctx;
    int unused;
} modbus_collector_runtime_t;

static modbus_collector_runtime_t g_runtime;

int modbus_collector_init(
        modbus_collector_t *collector,
        const app_config_t *config,
        telemetry_state_t *telemetry_state,
        modbus_simulator_t *simulator) {
    collector->config = *config;
    collector->telemetry_state = telemetry_state;
    collector->simulator = simulator;
    collector->connected = false;
    g_runtime.ctx = modbus_new_tcp(config->modbus_bind_host, config->modbus_bind_port);
    if (!g_runtime.ctx) {
        return -1;
    }
    modbus_set_response_timeout(g_runtime.ctx, 1, 0);
    return 0;
}

void modbus_collector_close(modbus_collector_t *collector) {
    (void) collector;
    if (g_runtime.ctx) {
        if (collector->connected) {
            modbus_close(g_runtime.ctx);
        }
        modbus_free(g_runtime.ctx);
        g_runtime.ctx = NULL;
    }
}

void modbus_collector_poll(modbus_collector_t *collector) {
    uint16_t values[2];
    int read_count;

    if (!collector->connected) {
        if (modbus_connect(g_runtime.ctx) == -1) {
            fprintf(stderr, "modbus master connect failed: %s\n", modbus_strerror(errno));
            return;
        }
        collector->connected = true;
    }

    read_count = modbus_read_input_registers(
            g_runtime.ctx,
            collector->config.register_temperature,
            2,
            values);
    if (read_count != 2) {
        fprintf(stderr, "modbus master read failed: %s\n", modbus_strerror(errno));
        modbus_close(g_runtime.ctx);
        collector->connected = false;
        return;
    }

    telemetry_state_update(
            collector->telemetry_state,
            values[0] / 10.0,
            values[1] / 10.0);

    printf("modbus master collected temperature=%.1f humidity=%.1f\n",
           values[0] / 10.0,
           values[1] / 10.0);
}
