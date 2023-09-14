package utils;

import models.devices.ControllingDevice;
import models.devices.Device;
import models.signals.controllingdevice.io.ControllingDeviceInputGetSignal;
import models.signals.controllingdevice.io.ControllingDeviceInputOutSignal;
import models.signals.controllingdevice.io.ControllingDeviceOutputSignal;
import models.signals.device.io.DeviceInputSignal;
import models.signals.device.io.DeviceOutputSignal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import threads.LoggingThread;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

public class SignalConverter {

    private static final byte DEVICE_INPUT_PACKET_WELCOME_BYTE = 0x50;

    private static final byte DEVICE_OUTPUT_PACKET_SIGNAL_BYTE = 0x51;

    private static final byte CONTROLLING_DEVICE_INPUT_PACKET_GOING_THROUGH = 0x47;
    private static final byte CONTROLLING_DEVICE_INPUT_PACKET_GET_ALL_DEVICE_INFO = 0x48;
    private static final byte CONTROLLING_DEVICE_INPUT_PACKET_GET_DEVICE_INFO = 0x49;

    @Contract("_ -> new")
    public static @NotNull InputSignal packetToSignal(DatagramPacket packet) {
        return new InputSignal(packet);
    }

    @Contract("_ -> new")
    public static @NotNull DeviceOutputSignal deviceOutputSignal(@NotNull Device device) {
        byte[] payload = device.toString().getBytes(StandardCharsets.UTF_8);
        byte[] fullPayload = new byte[payload.length + 1];
        System.arraycopy(payload, 0, fullPayload, 1, payload.length);
        fullPayload[0] = DEVICE_OUTPUT_PACKET_SIGNAL_BYTE;
        return new DeviceOutputSignal(fullPayload, device);
    }

    @Contract("_ -> new")
    public static @NotNull DeviceOutputSignal controllingInputToDeviceOutput(
            @NotNull ControllingDeviceInputOutSignal signal) {
        return new DeviceOutputSignal(signal.payload(), signal.targetDevice());
    }

    private static byte @NotNull [] getAllDevicesInfoPayload() {
        StringBuilder builder = new StringBuilder();
        if (SharedState.devices.size() < 1) {
            byte[] payload = new byte[1];
            payload[0] = 0x32;
            return payload;
        }
        for (Device device : SharedState.devices) {
            builder.append(device);
            builder.append(" /%!/ ");
        }
        byte[] payload = builder.toString().getBytes(StandardCharsets.UTF_8);
        byte[] fullPayload = new byte[payload.length + 1];
        System.arraycopy(payload, 0, fullPayload, 1, payload.length);
        fullPayload[0] = 0x35;
        return fullPayload;
    }

    @Contract("_ -> new")
    public static @NotNull ControllingDeviceOutputSignal controllingInputToControllingOutput(
            @NotNull ControllingDeviceInputGetSignal signal) {
        if (signal.getDevice() == null) {
            return new ControllingDeviceOutputSignal(signal.requestingDevice(), getAllDevicesInfoPayload());
        }
        byte[] payload = signal.getDevice().toString().getBytes(StandardCharsets.UTF_8);
        byte[] fullPayload = new byte[payload.length + 1];
        System.arraycopy(payload, 0, fullPayload, 1, payload.length);
        fullPayload[0] = 0x36;
        return new ControllingDeviceOutputSignal(signal.requestingDevice(), fullPayload);
    }

    public static class InputSignal {

        private DeviceInputSignal deviceInputSignal;
        private ControllingDeviceInputOutSignal controllingDeviceInputOutSignal;
        private ControllingDeviceInputGetSignal controllingDeviceInputGetSignal;

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
            } else if (payload[0] == CONTROLLING_DEVICE_INPUT_PACKET_GOING_THROUGH) {
                Device targetDevice = DeviceManager.getInstance().deserializeFromPayload(payload);
                if (targetDevice == null) {
                    return;
                }
                if (targetDevice.getId() == 0) {
                    return;
                }
                LoggingThread.log("Searching for Device with ID '" + targetDevice.getId() + "'...");
                Device foundDevice = DBUtil.getInstance().findDeviceById(targetDevice.getId());
                if (foundDevice != null) {
                    controllingDeviceInputOutSignal =
                            new ControllingDeviceInputOutSignal(foundDevice, payload);
                }
                LoggingThread.logError("Cannot find Device with ID '" + targetDevice.getId()
                        + "'. Packet was not sent.");
            } else if (payload[0] == CONTROLLING_DEVICE_INPUT_PACKET_GET_ALL_DEVICE_INFO) {
                controllingDeviceInputGetSignal =
                        new ControllingDeviceInputGetSignal(null, new ControllingDevice(address));
            } else if (payload[0] == CONTROLLING_DEVICE_INPUT_PACKET_GET_DEVICE_INFO) {
                Device targetDevice = DeviceManager.getInstance().deserializeFromPayload(payload);
                if (targetDevice == null) {
                    return;
                }
                if (targetDevice.getId() == 0) {
                    return;
                }
                LoggingThread.log("Searching for Device with ID '" + targetDevice.getId() + "'.");
                Device foundDevice = DBUtil.getInstance().findDeviceById(targetDevice.getId());
                if (foundDevice != null) {
                    controllingDeviceInputGetSignal =
                            new ControllingDeviceInputGetSignal(foundDevice, new ControllingDevice(address));
                }
                LoggingThread.logError("Cannot find Device with ID '" + targetDevice.getId()
                        + "'. Packet was not sent.");
            } else {
                LoggingThread.logWarning("Cannot identify packet from '" + packet.getAddress().toString() + "'.");
            }
        }

        public DeviceInputSignal getDeviceInputSignal() {
            return deviceInputSignal;
        }

        public ControllingDeviceInputOutSignal getControllingDeviceInputOutSignal() {
            return controllingDeviceInputOutSignal;
        }

        public ControllingDeviceInputGetSignal getControllingDeviceInputGetSignal() {
            return controllingDeviceInputGetSignal;
        }
    }

}
