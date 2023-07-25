package models.signals.controllingdevice.io;

import models.devices.ControllingDevice;

public record ControllingDeviceInputSignal(ControllingDevice controllingDevice, byte[] payload) { }
