#ifndef MOCK_DEVICE_MODBUS_TELEMETRY_STATE_H
#define MOCK_DEVICE_MODBUS_TELEMETRY_STATE_H

#include <pthread.h>

typedef struct telemetry_state {
    const char *device_id;
    double temperature;
    double humidity;
    int report_interval_ms;
    unsigned long version;
    pthread_mutex_t mutex;
} telemetry_state_t;

void telemetry_state_init(telemetry_state_t *state, const char *device_id, int report_interval_ms);
void telemetry_state_update(telemetry_state_t *state, double temperature, double humidity);
void telemetry_state_set_report_interval(telemetry_state_t *state, int report_interval_ms);
int telemetry_state_get_report_interval(const telemetry_state_t *state);
void telemetry_state_get_snapshot(const telemetry_state_t *state,
                                  double *temperature,
                                  double *humidity,
                                  unsigned long *version);

#endif
