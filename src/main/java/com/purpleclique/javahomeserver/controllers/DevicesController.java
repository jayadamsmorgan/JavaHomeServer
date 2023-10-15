package com.purpleclique.javahomeserver.controllers;

import com.purpleclique.javahomeserver.models.devices.Device;
import com.purpleclique.javahomeserver.models.dto.DeviceDTO;
import com.purpleclique.javahomeserver.utils.DBUtil;
import com.purpleclique.javahomeserver.utils.SharedState;
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
        for (Device device : SharedState.devices) {
            result.add(DeviceDTO.builder()
                            .deviceType(device.getClass().getSimpleName())
                            .device(device)
                    .build());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable int deviceId) {
        for (Device device : SharedState.devices) {
            if (device.getId() == deviceId) {
                return ResponseEntity.ok(
                        DeviceDTO.builder()
                                .device(device)
                                .deviceType(device.getClass().getSimpleName())
                        .build()
                );
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/{deviceId}")
    public ResponseEntity<HttpStatus> updateDevice(
            @PathVariable int deviceId, @RequestBody DeviceDTO body) {
        if (body == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        var targetDevice = DBUtil.getInstance().findDeviceById(deviceId);
        if (targetDevice.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        System.out.println(body.getDevice().toString());
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
