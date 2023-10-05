package com.purpleclique.javahomeserver.models.io;

import com.purpleclique.javahomeserver.models.devices.Device;

public record DeviceInputSignal(Device inputDevice, Device deviceToBeUpdated) { }
