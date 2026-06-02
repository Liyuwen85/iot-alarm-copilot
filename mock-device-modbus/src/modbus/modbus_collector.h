#ifndef MOCK_DEVICE_MODBUS_COLLECTOR_H
#define MOCK_DEVICE_MODBUS_COLLECTOR_H

#include <stdbool.h>
#include <stdint.h>

#include "app/app_config.h"
#include "modbus/modbus_simulator.h"
#include "telemetry/telemetry_state.h"

typedef struct modbus_collector {
    app_config_t config;
    telemetry_state_t *telemetry_state;
    modbus_simulator_t *simulator;
    bool connected;
} modbus_collector_t;

int modbus_collector_init(
        modbus_collector_t *collector,
        const app_config_t *config,
        telemetry_state_t *telemetry_state,
        modbus_simulator_t *simulator);
void modbus_collector_close(modbus_collector_t *collector);
void modbus_collector_poll(modbus_collector_t *collector);

#endif
