#include "modbus/modbus_simulator.h"

#include <errno.h>
#include <math.h>
#include <stdio.h>
#include <string.h>
#include <modbus/modbus-tcp.h>
#include <modbus/modbus.h>
#include <sys/socket.h>
#include <unistd.h>

static void *modbus_server_main(void *arg) {
    modbus_simulator_t *simulator = (modbus_simulator_t *) arg;
    modbus_t *ctx = modbus_new_tcp(simulator->config.modbus_bind_host,
                                   simulator->config.modbus_bind_port);
    modbus_mapping_t *mapping = NULL;
    uint8_t request[MODBUS_MAX_ADU_LENGTH];

    if (!ctx) {
        fprintf(stderr, "modbus simulator create ctx failed\n");
        return NULL;
    }

    mapping = modbus_mapping_new_start_address(0, 0,
                                               0, 0,
                                               0, 0,
                                               simulator->config.register_temperature,
                                               2);
    if (!mapping) {
        fprintf(stderr, "modbus simulator mapping failed: %s\n", modbus_strerror(errno));
        modbus_free(ctx);
        return NULL;
    }

    simulator->server_socket = modbus_tcp_listen(ctx, 1);
    if (simulator->server_socket < 0) {
        fprintf(stderr, "modbus simulator listen failed: %s\n", modbus_strerror(errno));
        modbus_mapping_free(mapping);
        modbus_free(ctx);
        return NULL;
    }

    printf("modbus simulator listening host=%s port=%d\n",
           simulator->config.modbus_bind_host,
           simulator->config.modbus_bind_port);

    while (simulator->running) {
        int accepted_socket = modbus_tcp_accept(ctx, &simulator->server_socket);
        if (accepted_socket < 0) {
            if (simulator->running) {
                fprintf(stderr, "modbus simulator accept failed: %s\n", modbus_strerror(errno));
            }
            continue;
        }

        while (simulator->running) {
            int received;

            pthread_mutex_lock(&simulator->mutex);
            mapping->tab_input_registers[0] = simulator->temperature_raw;
            mapping->tab_input_registers[1] = simulator->humidity_raw;
            pthread_mutex_unlock(&simulator->mutex);

            received = modbus_receive(ctx, request);
            if (received > 0) {
                modbus_reply(ctx, request, received, mapping);
            } else if (received == -1) {
                break;
            }
        }

        close(modbus_get_socket(ctx));
        modbus_set_socket(ctx, -1);
    }

    if (simulator->server_socket >= 0) {
        close(simulator->server_socket);
        simulator->server_socket = -1;
    }
    modbus_mapping_free(mapping);
    modbus_free(ctx);
    return NULL;
}

int modbus_simulator_init(modbus_simulator_t *simulator, const app_config_t *config) {
    memset(simulator, 0, sizeof(*simulator));
    simulator->config = *config;
    simulator->tick = 0;
    simulator->server_socket = -1;
    simulator->temperature_raw = 250;
    simulator->humidity_raw = 650;
    if (pthread_mutex_init(&simulator->mutex, NULL)) {
        return -1;
    }
    return 0;
}

int modbus_simulator_start(modbus_simulator_t *simulator) {
    simulator->running = true;
    if (pthread_create(&simulator->thread_handle, NULL, modbus_server_main, simulator)) {
        simulator->running = false;
        return -1;
    }
    return 0;
}

void modbus_simulator_stop(modbus_simulator_t *simulator) {
    simulator->running = false;
    if (simulator->server_socket >= 0) {
        shutdown(simulator->server_socket, SHUT_RDWR);
        close(simulator->server_socket);
        simulator->server_socket = -1;
    }
    if (simulator->thread_handle) {
        pthread_join(simulator->thread_handle, NULL);
        simulator->thread_handle = 0;
    }
    pthread_mutex_destroy(&simulator->mutex);
}

void modbus_simulator_tick(modbus_simulator_t *simulator) {
    double temperature;
    double humidity;

    simulator->tick += 1;
    temperature = 24.0 + sin((double) simulator->tick / 4.0) * 4.5;
    humidity = 55.0 + cos((double) simulator->tick / 5.0) * 10.0;

    pthread_mutex_lock(&simulator->mutex);
    simulator->temperature_raw = (uint16_t) (temperature * 10.0);
    simulator->humidity_raw = (uint16_t) (humidity * 10.0);
    pthread_mutex_unlock(&simulator->mutex);

    printf("modbus slave produced temperature=%.1f humidity=%.1f\n",
           simulator->temperature_raw / 10.0,
           simulator->humidity_raw / 10.0);
}

uint16_t modbus_simulator_temperature_raw(modbus_simulator_t *simulator) {
    uint16_t value;
    pthread_mutex_lock(&simulator->mutex);
    value = simulator->temperature_raw;
    pthread_mutex_unlock(&simulator->mutex);
    return value;
}

uint16_t modbus_simulator_humidity_raw(modbus_simulator_t *simulator) {
    uint16_t value;
    pthread_mutex_lock(&simulator->mutex);
    value = simulator->humidity_raw;
    pthread_mutex_unlock(&simulator->mutex);
    return value;
}
