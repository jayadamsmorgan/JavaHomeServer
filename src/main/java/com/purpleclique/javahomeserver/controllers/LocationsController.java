package com.purpleclique.javahomeserver.controllers;

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
        Set<LocationDTO> locations = new HashSet<>();
        // TODO: seems like not the best algorithm below... OK for now though
        for (Device device : SharedState.devices) {
            var location = locations.stream()
                    .filter(locationDTO -> locationDTO.getLocationName().equals(device.getLocation())).findFirst();
            if (location.isEmpty()) {
                var deviceDTO = DeviceDTO.builder()
                        .device(device)
                        .deviceType(device.getClass().getSimpleName())
                        .build();
                Set<DeviceDTO> devices = new HashSet<>();
                devices.add(deviceDTO);
                var newLocation = LocationDTO.builder()
                        .locationName(device.getLocation())
                        .devices(devices)
                        .build();
                locations.add(newLocation);
                continue;
            }
            var devices = location.get().getDevices();
            var deviceDTO = DeviceDTO.builder()
                    .device(device)
                    .deviceType(device.getClass().getSimpleName())
                    .build();
            devices.add(deviceDTO);
            location.get().setDevices(devices);
        }
        return ResponseEntity.ok(locations);
    }

}
