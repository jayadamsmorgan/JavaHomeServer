package com.purpleclique.javahomeserver.utils;

import com.purpleclique.javahomeserver.models.devices.Device;
import com.purpleclique.javahomeserver.models.io.DeviceInputSignal;
import com.purpleclique.javahomeserver.models.io.DeviceOutputSignal;
import com.purpleclique.javahomeserver.threads.LoggingThread;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

public class SignalConverter {

    private static final byte DEVICE_INPUT_PACKET_WELCOME_BYTE = 0x50;

    private static final byte DEVICE_OUTPUT_PACKET_WELCOME_BYTE = 0x51;


    @Contract("_ -> new")
    public static @NotNull InputSignal packetToSignal(DatagramPacket packet) {
        return new InputSignal(packet);
    }

    @Contract("_ -> new")
    public static @NotNull DeviceOutputSignal deviceOutputSignal(@NotNull Device device) {
        byte[] payload = device.toString().getBytes(StandardCharsets.UTF_8);
        byte[] fullPayload = new byte[payload.length + 1];
        System.arraycopy(payload, 0, fullPayload, 1, payload.length);
        fullPayload[0] = DEVICE_OUTPUT_PACKET_WELCOME_BYTE;
        return new DeviceOutputSignal(fullPayload, device);
    }

    public static class InputSignal {

        private DeviceInputSignal deviceInputSignal;

        private InputSignal(@NotNull DatagramPacket packet) {
            byte[] payload = packet.getData();
            String address = packet.getAddress().getHostAddress();
            if (payload[0] == DEVICE_INPUT_PACKET_WELCOME_BYTE) {
                Device sourceDevice = DeviceManager.getInstance().deserializeFromPayload(payload);
                if (sourceDevice == null) {
                    return;
                }
                sourceDevice.setIpAddress(address);
                LoggingThread.log("Searching for device...");
                Device foundDevice = DBUtil.getInstance().findDeviceByIpAddress(address);
                if (foundDevice != null) {
                    LoggingThread.log("Device found, updating...");
                    foundDevice.setIpAddress(address);
                    sourceDevice.setId(foundDevice.getId());
                    deviceInputSignal = new DeviceInputSignal(sourceDevice, foundDevice);
                    return;
                }
                foundDevice = DBUtil.getInstance().findDeviceById(sourceDevice.getId());
                if (foundDevice != null) {
                    LoggingThread.log("Device found (it's address has been changed), updating...");
                    foundDevice.setIpAddress(address);
                    deviceInputSignal = new DeviceInputSignal(sourceDevice, foundDevice);
                    return;
                }
                LoggingThread.log("Device was not found, trying to register new Device...");
                deviceInputSignal = new DeviceInputSignal(sourceDevice, null);
            } else {
                LoggingThread.logWarning("Cannot identify packet from '" + packet.getAddress().toString() + "'.");
            }
        }

        public DeviceInputSignal getDeviceInputSignal() {
            return deviceInputSignal;
        }

    }

}
