package com.purpleclique.javahomeserver.models.location;

import com.purpleclique.javahomeserver.models.devices.Device;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

public class Location {

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String locationName;

    @Getter
    @Setter
    private Set<Device> devices;

}
