package utils;

import models.devices.ControllingDevice;
import models.devices.Device;
import models.devices.sensors.Sensor;
import models.signals.controllingdevice.io.ControllingDeviceInputSignal;
import models.signals.controllingdevice.io.ControllingDeviceOutputSignal;
import models.signals.controllingdevice.requests.BasicRequest;
import models.signals.controllingdevice.requests.Request;
import models.signals.device.io.DeviceInputSignal;
import models.signals.device.io.DeviceOutputSignal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import threads.LoggingThread;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

public class SignalConverter {

    private static final byte DEVICE_INPUT_PACKET_WELCOME_BYTE = 0x32;
    private static final byte CONTROLLING_DEVICE_INPUT_PACKET_WELCOME_BYTE = 0x33;

    private static final byte CONTROLLING_DEVICE_INPUT_PACKET_GOING_THROUGH = 0x47;

    private static final byte CONTROLLING_DEVICE_INPUT_PACKET_GET_ALL_DEVICE_INFO = 0x48;
    private static final byte CONTROLLING_DEVICE_INPUT_PACKET_GET_DEVICE_INFO = 0x49;
    private static final byte CONTROLLING_DEVICE_INPUT_PACKET_GET_SENSOR_DATA = 0x50;

    @Contract("_ -> new")
    public static @NotNull SignalConverter.InputSignal packetToSignal(DatagramPacket packet) {
        return new InputSignal(packet);
    }

    public static @Nullable DeviceOutputSignal controllingInputToDeviceOutput(
            @NotNull ControllingDeviceInputSignal signal) {
        byte[] payload = new byte[NetworkManager.RECEIVE_PACKET_MAX_LENGTH - 2];
        System.arraycopy(signal.payload(), 2, payload, 0, payload.length);
        String payloadString = new String(payload, StandardCharsets.UTF_8).replace("{", ";")
                .replace("}", ";");
        String[] payloadData = payloadString.split(";");
        try {
            for (int i = 0; i < payloadData.length; i++) {
                if (payloadData[i].equalsIgnoreCase("ID")) {
                    String targetDeviceId = payloadData[i + 1];
                    for (Device device : SharedState.devices) {
                        if (targetDeviceId.equalsIgnoreCase(device.getId())) {
                            return new DeviceOutputSignal(signal.payload(), device, signal.controllingDevice());
                        }
                    }
                    LoggingThread.logError("Cannot forward data from '" + signal.controllingDevice().deviceAddress()
                            + "'. Device with id '" + targetDeviceId + " was not found.");
                    return null;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            LoggingThread.logError("Cannot forward data from '" + signal.controllingDevice().deviceAddress()
                    + "'. Invalid payload data.");
            return null;
        }
        LoggingThread.logError("Cannot forward data from '" + signal.controllingDevice().deviceAddress()
                + "'. Invalid payload data.");
        return null;
    }

    public static @Nullable Request controllingInputToRequest(
            @NotNull ControllingDeviceInputSignal signal) {
        Request request = new BasicRequest(signal.controllingDevice());
        switch (signal.payload()[1]) {
            case CONTROLLING_DEVICE_INPUT_PACKET_GET_ALL_DEVICE_INFO -> {
                request.setRequestType(Request.RequestType.REQUEST_TYPE_GET_ALL_DEVICES_INFO);
                return request;
            }
            case CONTROLLING_DEVICE_INPUT_PACKET_GET_DEVICE_INFO -> {
                request.setRequestType(Request.RequestType.REQUEST_TYPE_GET_DEVICE_INFO);
                Device device = getTargetDeviceFromPayload(signal.payload());
                if (device != null) {
                    request.setTargetDevice(device);
                    return request;
                }
                return null;
            }
            case CONTROLLING_DEVICE_INPUT_PACKET_GET_SENSOR_DATA -> {
                request.setRequestType(Request.RequestType.REQUEST_TYPE_GET_SENSOR_DATA);
                Device device = getTargetDeviceFromPayload(signal.payload());
                if (device == null) {
                    return null;
                }
                if (!(device instanceof Sensor)) {
                    LoggingThread.logError("Cannot get Sensor data from Device with ID '" + device.getId()
                            + "': Device is not a Sensor.");
                    return null;
                }
                request.setTargetDevice(device);
                return request;
            }
            default -> {
                LoggingThread.logError("Cannot get ControllingInputRequest: Wrong payload data.");
                return null;
            }
        }
    }

    public static @Nullable ControllingDeviceOutputSignal getControllingOutputFromRequest(@NotNull Request request) {
        if (request.getRequestType() == null) {
            LoggingThread.logError("Cannot get ControllingDeviceOutput from Request: RequestType is null.");
            return null;
        }
        switch (request.getRequestType()) {
            case REQUEST_TYPE_GET_ALL_DEVICES_INFO -> {
                return new ControllingDeviceOutputSignal(request.getSourceControllingDevice(),
                        getAllDevicesInfoPayload());
            }
            case REQUEST_TYPE_GET_DEVICE_INFO -> {
                return new ControllingDeviceOutputSignal(request.getSourceControllingDevice(),
                        getDeviceInfoPayload(request.getTargetDevice()));
            }
            case REQUEST_TYPE_GET_SENSOR_DATA -> {
                return null; // TODO: implement
            }
        }
        return null;
    }

    private static byte @NotNull [] getDeviceInfoPayload(@NotNull Device device) {
        byte[] payload = device.toString().getBytes(StandardCharsets.UTF_8);
        byte[] fullPayload = new byte[payload.length + 1];
        System.arraycopy(payload, 0, fullPayload, 1, payload.length);
        fullPayload[0] = 0x34;
        return fullPayload;
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

    public static @Nullable Device getTargetDeviceFromPayload(byte @NotNull [] signalPayload) {
        byte[] payload = new byte[NetworkManager.RECEIVE_PACKET_MAX_LENGTH - 2];
        System.arraycopy(signalPayload, 2, payload, 0, payload.length);
        String payloadString = new String(payload, StandardCharsets.UTF_8);
        String[] payloadData = payloadString.split(";");
        for (String str : payloadData) {
            if (str.contains("ID")) {
                try {
                    String id = str.split("=")[1];
                    for (Device device : SharedState.devices) {
                        if (device.getId().equalsIgnoreCase(id)) {
                            return device;
                        }
                    }
                    LoggingThread.logError("Cannot get target Device from ControllingInputSignal:" +
                            " Cannot find device with ID '" + id + "'.");
                    return null;
                } catch (IndexOutOfBoundsException e) {
                    LoggingThread.logError("Cannot get target Device from ControllingInputSignal:" +
                            " Wrong payload data.");
                    return null;
                }
            }
        }
        LoggingThread.logError("Cannot get target Device from ControllingInputSignal: Wrong payload data.");
        return null;
    }

    public static boolean checkIfControllingDevicePacketIsGoingThrough(@NotNull ControllingDeviceInputSignal signal) {
        return signal.payload()[1] == CONTROLLING_DEVICE_INPUT_PACKET_GOING_THROUGH;
    }

    public static class InputSignal {

        private DeviceInputSignal deviceInputSignal;
        private ControllingDeviceInputSignal controllingDeviceInputSignal;

        private InputSignal(@NotNull DatagramPacket packet) {
            byte[] payload = packet.getData();
            String address = packet.getAddress().getHostAddress();
            if (payload[0] == DEVICE_INPUT_PACKET_WELCOME_BYTE) {
                Device sourceDevice = null;
                for (Device device : SharedState.devices) {
                    if (device.getIpAddress().equalsIgnoreCase(address)) {
                        sourceDevice = device;
                    }
                }
                if (sourceDevice != null) {
                    deviceInputSignal = new DeviceInputSignal(sourceDevice, payload);
                    return;
                }
                LoggingThread.logWarning("Device with IP '" + address +
                        "' is not registered, or its address has changed. Checking...");
                Device device = DeviceManager.getInstance().deviceChangedAddress(packet);
                if (device != null) {
                    deviceInputSignal = new DeviceInputSignal(device, payload);
                    return;
                }
                LoggingThread.log("Device was not found, trying to register new Device...");
                device = DeviceManager.getInstance().registerNewDevice(packet);
                if (device != null) {
                    deviceInputSignal = new DeviceInputSignal(device, payload);
                }
            } else if (payload[0] == CONTROLLING_DEVICE_INPUT_PACKET_WELCOME_BYTE) {
                controllingDeviceInputSignal =
                        new ControllingDeviceInputSignal(new ControllingDevice(address), payload);

            } else {
                LoggingThread.logWarning("Cannot identify packet from '" + packet.getAddress().toString() + "'.");
            }
        }

        public DeviceInputSignal getDeviceInputSignal() {
            return deviceInputSignal;
        }

        public ControllingDeviceInputSignal getControllingDeviceInputSignal() {
            return  controllingDeviceInputSignal;
        }
    }

}
