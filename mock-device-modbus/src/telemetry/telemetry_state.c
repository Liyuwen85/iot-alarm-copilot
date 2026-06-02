#include "telemetry/telemetry_state.h"

#include <string.h>

void telemetry_state_init(telemetry_state_t *state, const char *device_id, int report_interval_ms) {
    memset(state, 0, sizeof(*state));
    pthread_mutex_init(&state->mutex, NULL);
    state->device_id = device_id;
    state->temperature = 0.0;
    state->humidity = 0.0;
    state->report_interval_ms = report_interval_ms;
    state->version = 0UL;
}

void telemetry_state_update(telemetry_state_t *state, double temperature, double humidity) {
    pthread_mutex_lock(&state->mutex);
    state->temperature = temperature;
    state->humidity = humidity;
    state->version += 1UL;
    pthread_mutex_unlock(&state->mutex);
}

void telemetry_state_set_report_interval(telemetry_state_t *state, int report_interval_ms) {
    pthread_mutex_lock(&state->mutex);
    state->report_interval_ms = report_interval_ms;
    pthread_mutex_unlock(&state->mutex);
}

int telemetry_state_get_report_interval(const telemetry_state_t *state) {
    int report_interval_ms;
    pthread_mutex_lock((pthread_mutex_t *) &state->mutex);
    report_interval_ms = state->report_interval_ms;
    pthread_mutex_unlock((pthread_mutex_t *) &state->mutex);
    return report_interval_ms;
}

void telemetry_state_get_snapshot(const telemetry_state_t *state,
                                  double *temperature,
                                  double *humidity,
                                  unsigned long *version) {
    pthread_mutex_lock((pthread_mutex_t *) &state->mutex);
    if (temperature) {
        *temperature = state->temperature;
    }
    if (humidity) {
        *humidity = state->humidity;
    }
    if (version) {
        *version = state->version;
    }
    pthread_mutex_unlock((pthread_mutex_t *) &state->mutex);
}
