#ifndef MOCK_DEVICE_MODBUS_APP_CONFIG_H
#define MOCK_DEVICE_MODBUS_APP_CONFIG_H

typedef struct app_config {
    const char *device_id;
    const char *modbus_bind_host;
    int modbus_bind_port;
    int register_temperature;
    int register_humidity;
    // lwM2M上报间隔
    int poll_interval_ms;
    const char *lwm2m_endpoint;
    const char *lwm2m_server_uri;
} app_config_t;

app_config_t app_config_load(void);

#endif
