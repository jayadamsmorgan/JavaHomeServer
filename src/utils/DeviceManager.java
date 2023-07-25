package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import models.devices.Device;
import models.devices.lights.LightDevice;
import models.devices.lights.RGBStrip;
import models.devices.lights.RGBWStrip;
import models.devices.sensors.Sensor;
import models.signals.device.io.DeviceInputSignal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import threads.LoggingThread;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class DeviceManager {

    private final File deviceFileDirectory;

    private static DeviceManager instance;

    private DeviceManager() {
        deviceFileDirectory = new File("Devices");
        if (deviceFileDirectory.exists()) {
            if (deviceFileDirectory.isDirectory()) {
                return;
            }
            deviceFileDirectory.delete();
        }
        deviceFileDirectory.mkdir();
    }

    public static DeviceManager getInstance() {
        if (instance == null) {
            instance = new DeviceManager();
        }
        return instance;
    }

    public void saveDevice(@NotNull Device device) throws DeviceNotSavedException {
        File deviceTypeFile = new File(deviceFileDirectory, device.getClass().getCanonicalName() + "_" + device.getId());
        if (!deviceTypeFile.exists()) {
            try {
                deviceTypeFile.createNewFile();
            } catch (IOException e) {
                throw new DeviceNotSavedException(e.getMessage());
            }
        }
        try (FileWriter fileWriter = new FileWriter(deviceTypeFile)) {
            fileWriter.write(DeviceSerializer.serialize(device) + "\n");
            fileWriter.flush();
        } catch (IOException | DeviceSerializer.DeviceSerializationException e) {
            throw new DeviceNotSavedException(e.getMessage());
        }
    }

    public @Nullable Device deviceChangedAddress(@NotNull DatagramPacket packet) {
        LoggingThread.log("Checking if device already exists and changed IP Address...");
        Device device = SignalConverter.getTargetDeviceFromPayload(packet.getData());
        if (device == null) {
            LoggingThread.logWarning("Device was not found.");
            return null;
        }
        LoggingThread.log("Device with ID '" + device.getId() + "' found. Changing its address to a new one: '"
                + packet.getAddress().getHostAddress() + "'.");
        device.setIpAddress(packet.getAddress().getHostAddress());
        device.save();
        return device;
    }

    public void parseDeviceInputSignal(@NotNull DeviceInputSignal signal) {
        LoggingThread.log("Parsing DeviceInputSignal for Device with ID '" + signal.sourceDevice().getId() + "'...");
        byte[] payload = new byte[signal.payload().length - 1];
        System.arraycopy(signal.payload(), 1, payload, 0, payload.length);
        String payloadString = new String(payload, StandardCharsets.UTF_8);
        String[] payloadData = payloadString.split(";");
        String name = null;
        String location = null;
        String on = null;
        for (String payloadDataString : payloadData) {
            // TODO: add sensor data parsing
            if (payloadDataString.toUpperCase().contains("NAME")) {
                try {
                    name = payloadDataString.split("=")[1];
                    signal.sourceDevice().setName(name);
                } catch (IndexOutOfBoundsException e) {
                    LoggingThread.logError("Cannot get NAME property from DeviceInputSignal: Wrong payload data.");
                }
            }
            if (payloadDataString.toUpperCase().contains("LOCATION")) {
                try {
                    location = payloadDataString.split("=")[1];
                    signal.sourceDevice().setLocation(location);
                } catch (IndexOutOfBoundsException e) {
                    LoggingThread.logError("Cannot get LOCATION property from DeviceInputSignal: Wrong payload data.");
                }
            }
            if (payloadDataString.toUpperCase().contains("ON")) {
                try {
                    on = payloadDataString.split("=")[1];
                    if (on.trim().equalsIgnoreCase("TRUE")) {
                        signal.sourceDevice().turnOn();
                    } else {
                        signal.sourceDevice().turnOff();
                    }
                } catch (IndexOutOfBoundsException e) {
                    LoggingThread.logError("Cannot get ON property from DeviceInputSignal: Wrong payload data.");
                }
            }
        }
        if (name == null && location == null && on == null) {
            LoggingThread.logWarning("Nothing to update from DeviceInputSignal.");
        } else {
            signal.sourceDevice().save();
            LoggingThread.log("Parsing for Device with ID '" + signal.sourceDevice().getId() + "' is complete.");
        }
    }

    public @Nullable Device registerNewDevice(@NotNull DatagramPacket packet) {
        byte[] devicePayload = new byte[NetworkManager.RECEIVE_PACKET_MAX_LENGTH - 1];
        System.arraycopy(packet.getData(), 1, devicePayload, 0, devicePayload.length);
        String devicePayloadString = new String(devicePayload, StandardCharsets.UTF_8);
        try {
            String deviceClass = devicePayloadString.split(";")[0].toUpperCase();
            Device device = null;
            String ipAddress = packet.getAddress().toString();
            switch (deviceClass) { // TODO: 7/23/23 Not the best way to instantiate new Device, check for the better way
                case "SENSOR" -> device = new Sensor(ipAddress);
                case "LIGHT" -> device  = new LightDevice(ipAddress);
                case "RGBSTRIP" -> device = new RGBStrip(ipAddress);
                case "RGBWSTRIP" -> device = new RGBWStrip(ipAddress);
            }
            if (device == null) {
                LoggingThread.logError("Cannot register new device, wrong payload data.");
                return null;
            }
            saveDevice(device);
            SharedState.devices.add(device);
            return device;
        } catch (Exception e) {
            LoggingThread.logError("Cannot register new Device: " + e.getMessage());
        }
        return null;
    }

    public Set<Device> loadDevices() throws DeviceNotFoundException {
        Set<Device> devices = new HashSet<>();
        File[] deviceFiles = deviceFileDirectory.listFiles();
        if (deviceFiles == null || deviceFiles.length < 1) {
            return devices;
        }
        for (File deviceFile : deviceFiles) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(deviceFile.getPath())));
                String className = deviceFile.getName().split("_")[0];
                Class<Device> classCheckName = (Class<Device>) Class.forName(className);
                Device device = DeviceSerializer.deserialize(content, classCheckName);
                devices.add(device);
            } catch (Exception e) {
                throw new DeviceNotFoundException(e.getMessage());
            }
        }
        return devices;
    }

    public static class DeviceNotSavedException extends Exception {
        private DeviceNotSavedException(String message) {
            super(message);
        }
    }
    public static class DeviceNotFoundException extends Exception {
        private DeviceNotFoundException(String message) {
            super(message);
        }
    }

    private static class DeviceSerializer {

        private static final ObjectMapper mapper = new ObjectMapper();
        private static final ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();

        public static String serialize(@NotNull Device device) throws DeviceSerializationException {
            try {
                return objectWriter.writeValueAsString(device);
            } catch (JsonProcessingException e) {
                throw new DeviceSerializationException("Cannot serialize Device with ID: " + device.getId() + "\n" + e.getMessage());
            }
        }

        public static Device deserialize(String serializedDevice, Class<? extends Device> valueType) throws DeviceSerializationException {
            try {
                return mapper.readValue(serializedDevice, valueType);
            } catch (JsonProcessingException e) {
                throw new DeviceSerializationException("Cannot deserialize Device: " + "\n" + e.getMessage());
            }
        }

        public static class DeviceSerializationException extends Exception {
            private DeviceSerializationException(String message) {
                super(message);
            }
        }

    }

}
