#include "app/gateway_app.h"

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "app/app_config.h"
#include "lwm2m/anjay_adapter.h"
#include "modbus/modbus_collector.h"
#include "modbus/modbus_simulator.h"
#include "telemetry/telemetry_state.h"

int gateway_app_run(void) {
    app_config_t config = app_config_load();
    telemetry_state_t telemetry_state;
    modbus_simulator_t simulator;
    modbus_collector_t collector;
    anjay_adapter_t lwm2m_client;

    setvbuf(stdout, NULL, _IOLBF, 0);
    setvbuf(stderr, NULL, _IONBF, 0);

    telemetry_state_init(&telemetry_state, config.device_id, config.poll_interval_ms);

    if (modbus_simulator_init(&simulator, &config)
            || modbus_collector_init(&collector, &config, &telemetry_state, &simulator)
            || anjay_adapter_init(&lwm2m_client, &config, &telemetry_state)) {
        fprintf(stderr, "mock-device-modbus init failed\n");
        return EXIT_FAILURE;
    }

    printf("mock-device-modbus starting deviceId=%s serverUri=%s\n",
           config.device_id,
           config.lwm2m_server_uri);

    if (modbus_simulator_start(&simulator) || anjay_adapter_start(&lwm2m_client)) {
        fprintf(stderr, "mock-device-modbus startup failed\n");
        modbus_collector_close(&collector);
        modbus_simulator_stop(&simulator);
        anjay_adapter_stop(&lwm2m_client);
        return EXIT_FAILURE;
    }

    for (;;) {
        modbus_simulator_tick(&simulator);
        modbus_collector_poll(&collector);
        anjay_adapter_step(&lwm2m_client);
        struct timespec delay;
        int report_interval_ms = telemetry_state_get_report_interval(&telemetry_state);
        delay.tv_sec = report_interval_ms / 1000;
        delay.tv_nsec = (long) (report_interval_ms % 1000) * 1000000L;
        nanosleep(&delay, NULL);
    }
}
