package com.purpleclique.javahomeserver.controllers;

import com.purpleclique.javahomeserver.models.devices.Device;
import com.purpleclique.javahomeserver.models.dto.DeviceDTO;
import com.purpleclique.javahomeserver.threads.DeviceOutputThread;
import com.purpleclique.javahomeserver.utils.DBUtil;
import com.purpleclique.javahomeserver.utils.SignalConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/devices")
public class DevicesController {

    @GetMapping
    public ResponseEntity<Set<DeviceDTO>> getAllDevices() {
        Set<DeviceDTO> result = new HashSet<>();
        var devices = DBUtil.getInstance().getAllDevices();
        for (Device device : devices) {
            result.add(DeviceDTO.builder()
                            .deviceType(device.getClass().getSimpleName())
                            .device(device)
                    .build());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable String deviceId) {
        var device = DBUtil.getInstance().findDeviceById(deviceId);
        if (device.isPresent()) {
            return ResponseEntity.ok(DeviceDTO.builder()
                    .device(device.get())
                    .deviceType(device.getClass().getSimpleName())
                    .build());
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<HttpStatus> updateDevice(@RequestBody DeviceDTO body) {
        if (body == null || body.getDevice() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        var targetDevice = DBUtil.getInstance().findDeviceById(body.getDevice().getId());
        if (targetDevice.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        DeviceOutputThread.deviceOutputSignals.add(SignalConverter.deviceOutputSignal(body.getDevice()));
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
