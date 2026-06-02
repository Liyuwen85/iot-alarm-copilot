#include "lwm2m/anjay_adapter.h"

#include <stdio.h>
#include <string.h>
#include <pthread.h>
#include <anjay/anjay.h>
#include <anjay/ipso_objects_v2.h>
#include <anjay/security.h>
#include <anjay/server.h>
#include <avsystem/commons/avs_log.h>

#include "lwm2m/report_interval_object.h"

enum {
    TEMPERATURE_OID = 3303,
    HUMIDITY_OID = 3304,
    SINGLE_SENSOR_INSTANCE = 1
};

typedef struct anjay_runtime {
    anjay_t *anjay;
    report_interval_object_t report_interval_object;
} anjay_runtime_t;

static const anjay_ipso_v2_basic_sensor_meta_t TEMPERATURE_META = {
    .unit = "Cel",
    .min_max_measured_value_present = true,
    .min_range_value = -40.0,
    .max_range_value = 125.0
};

static const anjay_ipso_v2_basic_sensor_meta_t HUMIDITY_META = {
    .unit = "%RH",
    .min_max_measured_value_present = true,
    .min_range_value = 0.0,
    .max_range_value = 100.0
};

static int setup_security_object(anjay_t *anjay, const app_config_t *config) {
    const anjay_security_instance_t security_instance = {
        .ssid = 1,
        .server_uri = config->lwm2m_server_uri,
        .security_mode = ANJAY_SECURITY_NOSEC
    };
    anjay_iid_t iid = ANJAY_ID_INVALID;

    if (anjay_security_object_install(anjay)) {
        return -1;
    }
    return anjay_security_object_add_instance(anjay, &security_instance, &iid);
}

static int setup_server_object(anjay_t *anjay) {
    const anjay_server_instance_t server_instance = {
        .ssid = 1,
        .lifetime = 60,
        .default_min_period = -1,
        .default_max_period = -1,
        .disable_timeout = -1,
        .binding = "U"
    };
    anjay_iid_t iid = ANJAY_ID_INVALID;

    if (anjay_server_object_install(anjay)) {
        return -1;
    }
    return anjay_server_object_add_instance(anjay, &server_instance, &iid);
}

static int setup_sensor_objects(anjay_t *anjay, telemetry_state_t *telemetry_state) {
    double temperature;
    double humidity;

    telemetry_state_get_snapshot(telemetry_state, &temperature, &humidity, NULL);

    if (anjay_ipso_v2_basic_sensor_install(
                anjay, TEMPERATURE_OID, NULL, SINGLE_SENSOR_INSTANCE)
            || anjay_ipso_v2_basic_sensor_install(
                    anjay, HUMIDITY_OID, NULL, SINGLE_SENSOR_INSTANCE)) {
        return -1;
    }

    if (anjay_ipso_v2_basic_sensor_instance_add(
                anjay, TEMPERATURE_OID, 0, temperature, &TEMPERATURE_META)
            || anjay_ipso_v2_basic_sensor_instance_add(
                    anjay, HUMIDITY_OID, 0, humidity, &HUMIDITY_META)) {
        return -1;
    }
    return 0;
}

static int setup_report_interval_object(anjay_t *anjay,
                                        telemetry_state_t *telemetry_state,
                                        report_interval_object_t *object) {
    if (report_interval_object_init(object, telemetry_state)) {
        return -1;
    }
    return anjay_register_object(anjay, report_interval_object_definition(object));
}

static void *anjay_event_loop_thread(void *arg) {
    anjay_adapter_t *adapter = (anjay_adapter_t *) arg;
    anjay_runtime_t *runtime = (anjay_runtime_t *) adapter->anjay;

    avs_log_set_default_level(AVS_LOG_INFO);
    (void) anjay_event_loop_run(
            runtime->anjay, avs_time_duration_from_scalar(100, AVS_TIME_MS));
    return NULL;
}

int anjay_adapter_init(
        anjay_adapter_t *adapter,
        const app_config_t *config,
        telemetry_state_t *telemetry_state) {
    memset(adapter, 0, sizeof(*adapter));
    adapter->config = *config;
    adapter->telemetry_state = telemetry_state;
    adapter->last_sent_version = 0UL;

    {
        static anjay_runtime_t runtime_storage;
        const anjay_configuration_t anjay_config = {
            .endpoint_name = config->lwm2m_endpoint,
            .in_buffer_size = 4000,
            .out_buffer_size = 4000,
            .msg_cache_size = 4000
        };

        runtime_storage.anjay = anjay_new(&anjay_config);
        if (!runtime_storage.anjay) {
            return -1;
        }
        if (setup_security_object(runtime_storage.anjay, config)
                || setup_server_object(runtime_storage.anjay)
                || setup_sensor_objects(runtime_storage.anjay, telemetry_state)
                || setup_report_interval_object(
                        runtime_storage.anjay,
                        telemetry_state,
                        &runtime_storage.report_interval_object)) {
            anjay_delete(runtime_storage.anjay);
            runtime_storage.anjay = NULL;
            return -1;
        }
        adapter->anjay = &runtime_storage;
        return 0;
    }
}

int anjay_adapter_start(anjay_adapter_t *adapter) {
    adapter->running = true;
    printf("anjay client starting endpoint=%s server=%s\n",
           adapter->config.lwm2m_endpoint,
           adapter->config.lwm2m_server_uri);
    if (pthread_create(&adapter->thread_handle, NULL, anjay_event_loop_thread, adapter)) {
        adapter->running = false;
        return -1;
    }
    return 0;
}

void anjay_adapter_stop(anjay_adapter_t *adapter) {
    anjay_runtime_t *runtime = (anjay_runtime_t *) adapter->anjay;

    adapter->running = false;
    if (adapter->thread_handle) {
        pthread_cancel(adapter->thread_handle);
        pthread_join(adapter->thread_handle, NULL);
        adapter->thread_handle = 0;
    }
    if (runtime && runtime->anjay) {
        anjay_delete(runtime->anjay);
        runtime->anjay = NULL;
    }
}

void anjay_adapter_step(anjay_adapter_t *adapter) {
    double temperature;
    double humidity;
    unsigned long version;

    telemetry_state_get_snapshot(adapter->telemetry_state, &temperature, &humidity, &version);
    if (version == adapter->last_sent_version) {
        return;
    }

    adapter->last_sent_version = version;
    {
        anjay_runtime_t *runtime = (anjay_runtime_t *) adapter->anjay;

        if (!runtime || !runtime->anjay) {
            return;
        }
        (void) anjay_ipso_v2_basic_sensor_value_update(
                runtime->anjay, TEMPERATURE_OID, 0, temperature);
        (void) anjay_ipso_v2_basic_sensor_value_update(
                runtime->anjay, HUMIDITY_OID, 0, humidity);
        printf("anjay client reported /3303/0/5700=%.1f /3304/0/5700=%.1f version=%lu\n",
               temperature,
               humidity,
               adapter->last_sent_version);
    }
}
