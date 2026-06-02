#include "app/app_config.h"

#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static int is_running_in_wsl(void) {
    FILE *file;
    char line[256];

    if (getenv("WSL_INTEROP") != NULL || getenv("WSL_DISTRO_NAME") != NULL) {
        return 1;
    }

    file = fopen("/proc/version", "r");
    if (file == NULL) {
        return 0;
    }

    while (fgets(line, sizeof(line), file) != NULL) {
        if (strstr(line, "Microsoft") != NULL || strstr(line, "microsoft") != NULL) {
            fclose(file);
            return 1;
        }
    }

    fclose(file);
    return 0;
}

// 返回宿主ip
static int try_read_wsl_gateway_ip(char *buffer, size_t buffer_size) {
    FILE *file;
    char line[256];

    file = fopen("/proc/net/route", "r");
    if (file == NULL) {
        return 0;
    }

    if (fgets(line, sizeof(line), file) == NULL) {
        fclose(file);
        return 0;
    }

    while (fgets(line, sizeof(line), file) != NULL) {
        char iface[64];
        unsigned long destination = 0UL;
        unsigned long gateway = 0UL;
        unsigned long flags = 0UL;
        int parsed;
        unsigned int octet1;
        unsigned int octet2;
        unsigned int octet3;
        unsigned int octet4;

        parsed = sscanf(line, "%63s %lx %lx %lx", iface, &destination, &gateway, &flags);
        if (parsed < 4 || destination != 0UL || gateway == 0UL) {
            continue;
        }

        octet1 = (unsigned int) (gateway & 0xFFUL);
        octet2 = (unsigned int) ((gateway >> 8U) & 0xFFUL);
        octet3 = (unsigned int) ((gateway >> 16U) & 0xFFUL);
        octet4 = (unsigned int) ((gateway >> 24U) & 0xFFUL);

        if (snprintf(buffer, buffer_size, "%u.%u.%u.%u",
                     octet1, octet2, octet3, octet4) < (int) buffer_size) {
            fclose(file);
            return 1;
        }
    }

    fclose(file);
    return 0;
}

static int try_read_wsl_nameserver_ip(char *buffer, size_t buffer_size) {
    FILE *file;
    char line[256];

    file = fopen("/etc/resolv.conf", "r");
    if (file == NULL) {
        return 0;
    }

    while (fgets(line, sizeof(line), file) != NULL) {
        char host_ip[64];

        if (sscanf(line, "nameserver %63s", host_ip) != 1) {
            continue;
        }
        if (snprintf(buffer, buffer_size, "%s", host_ip) < (int) buffer_size) {
            fclose(file);
            return 1;
        }
    }

    fclose(file);
    return 0;
}

static const char *default_lwm2m_server_uri(void) {
    static char uri[96];
    static int initialized = 0;

    if (!initialized) {
        char host_ip[64];

        initialized = 1;

        /*
         * In WSL, 127.0.0.1 points to the Linux VM itself.
         * The Java gateway is usually running on the Windows host.
         */
        if (is_running_in_wsl()
                && (try_read_wsl_gateway_ip(host_ip, sizeof(host_ip))
                || try_read_wsl_nameserver_ip(host_ip, sizeof(host_ip)))) {
            if (snprintf(uri, sizeof(uri), "coap://%s:5683", host_ip) < (int) sizeof(uri)) {
                return uri;
            }
        }

        (void) snprintf(uri, sizeof(uri), "coap://127.0.0.1:5683");
    }

    return uri;
}

static const char *env_or_default(const char *key, const char *default_value) {
    const char *value = getenv(key);
    if (value == NULL || value[0] == '\0') {
        return default_value;
    }
    return value;
}

static int env_int_or_default(const char *key, int default_value) {
    char *end = NULL;
    const char *value = getenv(key);
    long parsed;

    if (value == NULL || value[0] == '\0') {
        return default_value;
    }

    parsed = strtol(value, &end, 10);
    if (end == value || *end != '\0') {
        return default_value;
    }
    if (parsed < INT_MIN || parsed > INT_MAX) {
        return default_value;
    }
    return (int) parsed;
}

app_config_t app_config_load(void) {
    app_config_t config;
    config.device_id = env_or_default("DEVICE_ID", "demo-002");
    config.modbus_bind_host = env_or_default("MODBUS_BIND_HOST", "127.0.0.1");
    config.modbus_bind_port = env_int_or_default("MODBUS_BIND_PORT", 15020);
    config.register_temperature = env_int_or_default("REGISTER_TEMPERATURE", 0);
    config.register_humidity = env_int_or_default("REGISTER_HUMIDITY", 1);
    config.poll_interval_ms = env_int_or_default("POLL_INTERVAL_MS", 5000);
    config.lwm2m_endpoint = env_or_default("LWM2M_ENDPOINT", "demo-002");
    config.lwm2m_server_uri = env_or_default("LWM2M_SERVER_URI", default_lwm2m_server_uri());
    return config;
}
