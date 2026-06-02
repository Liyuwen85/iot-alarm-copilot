#ifndef MOCK_DEVICE_MODBUS_SIMULATOR_H
#define MOCK_DEVICE_MODBUS_SIMULATOR_H

#include <stdbool.h>
#include <stdint.h>
#include <pthread.h>

#include "app/app_config.h"

typedef struct modbus_simulator {
    app_config_t config;
    int tick;
    int server_socket;
    bool running;
    uint16_t temperature_raw;
    uint16_t humidity_raw;
    pthread_t thread_handle;
    pthread_mutex_t mutex;
} modbus_simulator_t;

int modbus_simulator_init(modbus_simulator_t *simulator, const app_config_t *config);
int modbus_simulator_start(modbus_simulator_t *simulator);
void modbus_simulator_stop(modbus_simulator_t *simulator);
void modbus_simulator_tick(modbus_simulator_t *simulator);
uint16_t modbus_simulator_temperature_raw(modbus_simulator_t *simulator);
uint16_t modbus_simulator_humidity_raw(modbus_simulator_t *simulator);

#endif
