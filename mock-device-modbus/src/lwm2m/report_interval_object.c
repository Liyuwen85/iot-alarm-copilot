#include "lwm2m/report_interval_object.h"

#include <assert.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>

enum {
    REPORT_INTERVAL_OBJECT_ID = 31024,
    REPORT_INTERVAL_INSTANCE_ID = 0,
    REPORT_INTERVAL_RESOURCE_ID = 1,
    REPORT_INTERVAL_MIN_MS = 500,
    REPORT_INTERVAL_MAX_MS = 600000
};

static report_interval_object_t *get_object(const anjay_dm_object_def_t *const *obj_ptr) {
    assert(obj_ptr);
    return AVS_CONTAINER_OF(obj_ptr, report_interval_object_t, obj_def);
}

static int list_instances(anjay_t *anjay,
                          const anjay_dm_object_def_t *const *obj_ptr,
                          anjay_dm_list_ctx_t *ctx) {
    (void) anjay;
    (void) obj_ptr;

    anjay_dm_emit(ctx, REPORT_INTERVAL_INSTANCE_ID);
    return 0;
}

static int list_resources(anjay_t *anjay,
                          const anjay_dm_object_def_t *const *obj_ptr,
                          anjay_iid_t iid,
                          anjay_dm_resource_list_ctx_t *ctx) {
    (void) anjay;
    (void) obj_ptr;

    if (iid != REPORT_INTERVAL_INSTANCE_ID) {
        return ANJAY_ERR_NOT_FOUND;
    }

    anjay_dm_emit_res(ctx,
                      REPORT_INTERVAL_RESOURCE_ID,
                      ANJAY_DM_RES_RW,
                      ANJAY_DM_RES_PRESENT);
    return 0;
}

static int resource_read(anjay_t *anjay,
                         const anjay_dm_object_def_t *const *obj_ptr,
                         anjay_iid_t iid,
                         anjay_rid_t rid,
                         anjay_riid_t riid,
                         anjay_output_ctx_t *ctx) {
    (void) anjay;

    report_interval_object_t *object = get_object(obj_ptr);

    if (iid != REPORT_INTERVAL_INSTANCE_ID || riid != ANJAY_ID_INVALID) {
        return ANJAY_ERR_NOT_FOUND;
    }
    if (rid != REPORT_INTERVAL_RESOURCE_ID) {
        return ANJAY_ERR_NOT_FOUND;
    }

    return anjay_ret_i32(ctx, telemetry_state_get_report_interval(object->telemetry_state));
}

static int resource_write(anjay_t *anjay,
                          const anjay_dm_object_def_t *const *obj_ptr,
                          anjay_iid_t iid,
                          anjay_rid_t rid,
                          anjay_riid_t riid,
                          anjay_input_ctx_t *ctx) {
    (void) anjay;

    report_interval_object_t *object = get_object(obj_ptr);
    int32_t value;
    int result;

    if (iid != REPORT_INTERVAL_INSTANCE_ID || riid != ANJAY_ID_INVALID) {
        return ANJAY_ERR_NOT_FOUND;
    }
    if (rid != REPORT_INTERVAL_RESOURCE_ID) {
        return ANJAY_ERR_NOT_FOUND;
    }

    result = anjay_get_i32(ctx, &value);
    if (result) {
        return result;
    }
    if (value < REPORT_INTERVAL_MIN_MS || value > REPORT_INTERVAL_MAX_MS) {
        return ANJAY_ERR_BAD_REQUEST;
    }

    telemetry_state_set_report_interval(object->telemetry_state, value);
    printf("lwm2m write received endpoint=%s resource=/31024/0/1 value=%d\n",
           object->telemetry_state->device_id,
           (int) value);
    return 0;
}

static const anjay_dm_object_def_t REPORT_INTERVAL_OBJECT_DEF = {
    .oid = REPORT_INTERVAL_OBJECT_ID,
    .handlers = {
        .list_instances = list_instances,
        .list_resources = list_resources,
        .resource_read = resource_read,
        .resource_write = resource_write,
        .transaction_begin = anjay_dm_transaction_NOOP,
        .transaction_validate = anjay_dm_transaction_NOOP,
        .transaction_commit = anjay_dm_transaction_NOOP,
        .transaction_rollback = anjay_dm_transaction_NOOP
    }
};

int report_interval_object_init(
        report_interval_object_t *object,
        telemetry_state_t *telemetry_state) {
    memset(object, 0, sizeof(*object));
    object->obj_def = &REPORT_INTERVAL_OBJECT_DEF;
    object->telemetry_state = telemetry_state;
    return 0;
}

const anjay_dm_object_def_t *const *report_interval_object_definition(
        report_interval_object_t *object) {
    return (const anjay_dm_object_def_t *const *) &object->obj_def;
}
