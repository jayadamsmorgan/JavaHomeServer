package models.signals.controllingdevice.io;

import models.devices.ControllingDevice;
import models.devices.Device;

public record ControllingDeviceInputGetSignal(Device getDevice, ControllingDevice requestingDevice) { }
