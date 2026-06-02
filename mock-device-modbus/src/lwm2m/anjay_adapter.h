#ifndef MOCK_DEVICE_MODBUS_ANJAY_ADAPTER_H
#define MOCK_DEVICE_MODBUS_ANJAY_ADAPTER_H

#include <stdbool.h>
#include <pthread.h>

#include "app/app_config.h"
#include "telemetry/telemetry_state.h"

typedef struct anjay_adapter {
    app_config_t config;
    telemetry_state_t *telemetry_state;
    unsigned long last_sent_version;
    bool running;
    void *anjay;
    pthread_t thread_handle;
} anjay_adapter_t;

int anjay_adapter_init(
        anjay_adapter_t *adapter,
        const app_config_t *config,
        telemetry_state_t *telemetry_state);
int anjay_adapter_start(anjay_adapter_t *adapter);
void anjay_adapter_stop(anjay_adapter_t *adapter);
void anjay_adapter_step(anjay_adapter_t *adapter);

#endif
