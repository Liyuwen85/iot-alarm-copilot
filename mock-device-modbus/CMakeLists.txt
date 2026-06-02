cmake_minimum_required(VERSION 3.22)

project(mock_device_modbus C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_C_EXTENSIONS OFF)

find_package(Threads REQUIRED)

set(WITH_DEMO OFF CACHE BOOL "" FORCE)
set(WITH_TEST OFF CACHE BOOL "" FORCE)
set(WITH_EXAMPLES OFF CACHE BOOL "" FORCE)
set(DTLS_BACKEND mbedtls CACHE STRING "" FORCE)

add_executable(mock-device-modbus
    src/main.c
    src/app/app_config.c
    src/app/gateway_app.c
    src/modbus/modbus_simulator.c
    src/modbus/modbus_collector.c
    src/telemetry/telemetry_state.c
    src/lwm2m/anjay_adapter.c
    src/lwm2m/report_interval_object.c
)

add_executable(mock-device-modbus-ddd
    src_ddd/main.c
    src_ddd/application/gateway_service.c
    src_ddd/domain/telemetry/report_interval.c
    src_ddd/domain/telemetry/telemetry_state_store.c
    src_ddd/infrastructure/config/runtime_config.c
    src_ddd/infrastructure/modbus/modbus_slave_adapter.c
    src_ddd/infrastructure/modbus/modbus_collector_adapter.c
    src_ddd/infrastructure/lwm2m/lwm2m_reporting_adapter.c
    src/modbus/modbus_simulator.c
    src/modbus/modbus_collector.c
    src/telemetry/telemetry_state.c
    src/lwm2m/anjay_adapter.c
    src/lwm2m/report_interval_object.c
)

target_include_directories(mock-device-modbus
    PRIVATE
        src
)

target_include_directories(mock-device-modbus-ddd
    PRIVATE
        src
        src_ddd
)

add_subdirectory(third_party/Anjay third_party/Anjay/build-project EXCLUDE_FROM_ALL)

if (MSVC)
    target_compile_options(mock-device-modbus PRIVATE /W4)
    target_compile_options(mock-device-modbus-ddd PRIVATE /W4)
else()
    target_compile_options(mock-device-modbus PRIVATE -Wall -Wextra -Wpedantic)
    target_compile_options(mock-device-modbus-ddd PRIVATE -Wall -Wextra -Wpedantic)
    target_compile_definitions(mock-device-modbus PRIVATE _POSIX_C_SOURCE=200809L)
    target_compile_definitions(mock-device-modbus-ddd PRIVATE _POSIX_C_SOURCE=200809L)
endif()

target_link_libraries(mock-device-modbus
    PRIVATE
        anjay
        Threads::Threads
        modbus
        m
)

target_link_libraries(mock-device-modbus-ddd
    PRIVATE
        anjay
        Threads::Threads
        modbus
        m
)
