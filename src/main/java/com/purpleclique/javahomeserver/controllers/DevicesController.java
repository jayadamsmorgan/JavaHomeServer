package com.purpleclique.javahomeserver.controllers;

import com.purpleclique.javahomeserver.models.devices.BasicDevice;
import com.purpleclique.javahomeserver.models.devices.Device;
import com.purpleclique.javahomeserver.utils.DBUtil;
import com.purpleclique.javahomeserver.utils.SharedState;
import com.purpleclique.javahomeserver.utils.SignalConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/device")
public class DevicesController {

    @GetMapping
    public ResponseEntity<Set<Device>> getAllDevices() {
        return ResponseEntity.ok(SharedState.devices);
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<Device> getDeviceById(@PathVariable int deviceId) {
        Device device = DBUtil.getInstance().findDeviceById(deviceId);
        if (device == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(device);
    }

    @PostMapping("/{deviceId}")
    public <T extends BasicDevice> ResponseEntity<HttpStatus> updateDevice(@PathVariable int deviceId, @RequestBody T body) {
        if (body == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Device targetDevice = DBUtil.getInstance().findDeviceById(deviceId);
        if (targetDevice == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        SharedState.deviceOutputSignals.add(SignalConverter.deviceOutputSignal(body));
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
