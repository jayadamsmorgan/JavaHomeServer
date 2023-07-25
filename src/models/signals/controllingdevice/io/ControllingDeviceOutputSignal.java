package models.signals.controllingdevice.io;

import models.devices.ControllingDevice;
import utils.NetworkManager;

public record ControllingDeviceOutputSignal(ControllingDevice device, byte[] payload) {

    public void send() {
        NetworkManager.getInstance().sendPacket(device.deviceAddress(), payload);
    }

}
