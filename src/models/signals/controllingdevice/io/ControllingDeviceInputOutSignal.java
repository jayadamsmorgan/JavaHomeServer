package models.signals.controllingdevice.io;

import models.devices.Device;

public record ControllingDeviceInputOutSignal(Device targetDevice, byte[] payload) { }
