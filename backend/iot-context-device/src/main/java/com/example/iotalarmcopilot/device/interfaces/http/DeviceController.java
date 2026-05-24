package com.example.iotalarmcopilot.device.interfaces.http;

import com.example.iotalarmcopilot.device.application.*;
import com.example.iotalarmcopilot.device.domain.model.Device;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * 设备控制器
 */
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceApplicationService deviceApplicationService;
    private final DeviceQueryApplicationService deviceQueryApplicationService;

    public DeviceController(
            DeviceApplicationService deviceApplicationService,
            DeviceQueryApplicationService deviceQueryApplicationService) {
        this.deviceApplicationService = deviceApplicationService;
        this.deviceQueryApplicationService = deviceQueryApplicationService;
    }

    @PostMapping
    public DeviceVO register(@RequestBody RegisterDeviceRequest request) {
        Device device = deviceApplicationService.register(new RegisterDeviceCommand(
                request.deviceCode(),
                request.productCode(),
                request.deviceName(),
                request.groupCode(),
                Instant.now()));
        return deviceQueryApplicationService.toVO(device);
    }

    @PostMapping("/{deviceCode}/activate")
    public DeviceVO activate(@PathVariable("deviceCode") String deviceCode) {
        Device device = deviceApplicationService.activate(new ActivateDeviceCommand(deviceCode, Instant.now()));
        return deviceQueryApplicationService.toVO(device);
    }

    @PostMapping("/{deviceCode}/maintenance/start")
    public DeviceVO startMaintenance(@PathVariable("deviceCode") String deviceCode) {
        Device device = deviceApplicationService.startMaintenance(new StartMaintenanceCommand(deviceCode, Instant.now()));
        return deviceQueryApplicationService.toVO(device);
    }

    @PostMapping("/{deviceCode}/maintenance/finish")
    public DeviceVO finishMaintenance(@PathVariable("deviceCode") String deviceCode) {
        Device device = deviceApplicationService.finishMaintenance(new FinishMaintenanceCommand(deviceCode, Instant.now()));
        return deviceQueryApplicationService.toVO(device);
    }

    @PostMapping("/{deviceCode}/disable")
    public DeviceVO disable(@PathVariable("deviceCode") String deviceCode) {
        Device device = deviceApplicationService.disable(new DisableDeviceCommand(deviceCode, Instant.now()));
        return deviceQueryApplicationService.toVO(device);
    }

    @PostMapping("/{deviceCode}/retire")
    public DeviceVO retire(@PathVariable("deviceCode") String deviceCode) {
        Device device = deviceApplicationService.retire(new RetireDeviceCommand(deviceCode, Instant.now()));
        return deviceQueryApplicationService.toVO(device);
    }

    @PostMapping("/{deviceCode}/group")
    public DeviceVO changeGroup(@PathVariable("deviceCode") String deviceCode, @RequestBody ChangeGroupRequest request) {
        Device device = deviceApplicationService.changeGroup(new ChangeDeviceGroupCommand(
                deviceCode,
                request.groupCode(),
                Instant.now()));
        return deviceQueryApplicationService.toVO(device);
    }

    @PostMapping("/{deviceCode}/shadow")
    public DeviceVO updateShadow(@PathVariable("deviceCode") String deviceCode, @RequestBody UpdateShadowRequest request) {
        Device device = deviceApplicationService.updateShadow(new UpdateDeviceShadowCommand(
                deviceCode,
                request.shadowDocument(),
                Instant.now()));
        return deviceQueryApplicationService.toVO(device);
    }

    @GetMapping("/{deviceCode}")
    public DeviceVO get(@PathVariable("deviceCode") String deviceCode) {
        return deviceQueryApplicationService.get(deviceCode);
    }

    @GetMapping("/recent")
    public List<DeviceVO> recent(@RequestParam(name = "limit", defaultValue = "20") int limit) {
        return deviceQueryApplicationService.recent(limit);
    }

    public record RegisterDeviceRequest(
            String deviceCode,
            String productCode,
            String deviceName,
            String groupCode) {
    }

    public record ChangeGroupRequest(String groupCode) {
    }

    public record UpdateShadowRequest(String shadowDocument) {
    }
}
