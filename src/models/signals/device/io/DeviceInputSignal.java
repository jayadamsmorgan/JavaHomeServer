package models.signals.device.io;

import models.devices.Device;

public record DeviceInputSignal(Device sourceDevice, byte[] payload) { }
