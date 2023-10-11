package com.purpleclique.javahomeserver.controllers;

import com.purpleclique.javahomeserver.models.devices.BasicDevice;
import com.purpleclique.javahomeserver.models.devices.Device;
import com.purpleclique.javahomeserver.models.dto.DeviceDTO;
import com.purpleclique.javahomeserver.models.dto.LocationDTO;
import com.purpleclique.javahomeserver.utils.SharedState;
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
        Set<DeviceDTO> deviceDTOS = new HashSet<>();
        for (Device device : SharedState.devices) {
            deviceDTOS.add(DeviceDTO.builder()
                            .deviceType(device.getClass().getSimpleName())
                            .device((BasicDevice) device)
                    .build());
        }
        LocationDTO locationDTO = LocationDTO.builder()
                .id(1)
                .locationName("testLocation")
                .devices(deviceDTOS)
                .build();
        Set<LocationDTO> locations = new HashSet<>();
        locations.add(locationDTO);
        return ResponseEntity.ok(locations);
    }

}
