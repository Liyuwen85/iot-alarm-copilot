#ifndef MOCK_DEVICE_MODBUS_REPORT_INTERVAL_OBJECT_H
#define MOCK_DEVICE_MODBUS_REPORT_INTERVAL_OBJECT_H

#include <anjay/anjay.h>

#include "telemetry/telemetry_state.h"

typedef struct report_interval_object {
    const anjay_dm_object_def_t *obj_def;
    telemetry_state_t *telemetry_state;
} report_interval_object_t;

int report_interval_object_init(
        report_interval_object_t *object,
        telemetry_state_t *telemetry_state);

const anjay_dm_object_def_t *const *report_interval_object_definition(
        report_interval_object_t *object);

#endif
