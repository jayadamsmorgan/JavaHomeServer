package com.purpleclique.javahomeserver.controllers;

import com.purpleclique.javahomeserver.models.devices.Device;
import com.purpleclique.javahomeserver.models.dto.DeviceDTO;
import com.purpleclique.javahomeserver.models.dto.LocationDTO;
import com.purpleclique.javahomeserver.utils.DBUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationsController {

    @GetMapping
    public ResponseEntity<Set<LocationDTO>> getLocations() {
        Set<LocationDTO> locationDTOS = new HashSet<>();
        var locations = DBUtil.getInstance().getLocations();
        for (var location : locations) {
            Set<Device> deviceSet = DBUtil.getInstance().getDevicesByLocation(location);
            Set<DeviceDTO> deviceDTOS = new HashSet<>();
            for (var device : deviceSet) {
                deviceDTOS.add(new DeviceDTO(device.getClass().getSimpleName(), device));
            }
            locationDTOS.add(new LocationDTO(location, deviceDTOS));
        }
        return ResponseEntity.ok(locationDTOS);
    }

}
