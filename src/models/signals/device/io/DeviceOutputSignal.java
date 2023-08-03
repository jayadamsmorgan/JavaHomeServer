package models.signals.device.io;

import models.devices.Device;
import utils.NetworkManager;

public record DeviceOutputSignal(byte[] payload, Device targetDevice) {

    public void send() {
        NetworkManager.getInstance().sendPacket(targetDevice.getIpAddress(), payload);
    }

}
