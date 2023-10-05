package com.purpleclique.javahomeserver.models.io;

import com.purpleclique.javahomeserver.models.devices.Device;
import com.purpleclique.javahomeserver.utils.NetworkManager;

public record DeviceOutputSignal(byte[] payload, Device targetDevice) {

    public void send() {
        NetworkManager.getInstance().sendPacket(targetDevice.getIpAddress(), payload);
    }

}
