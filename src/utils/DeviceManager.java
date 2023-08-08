package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import models.devices.Device;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import threads.LoggingThread;

import java.nio.charset.StandardCharsets;

public class DeviceManager {

    private static DeviceManager instance;

    private DeviceManager() {
    }

    public static DeviceManager getInstance() {
        if (instance == null) {
            instance = new DeviceManager();
        }
        return instance;
    }

    public void saveDevice(@NotNull Device device) {
        if (SharedState.devices.contains(device)) {
            DBUtil.getInstance().updateDevice(device);
        } else {
            DBUtil.getInstance().saveNewDevice(device);
        }
    }

    public @Nullable Device deserializeFromPayload(byte @NotNull [] payload) {
        try {
            byte[] payloadData = new byte[payload.length - 1];
            System.arraycopy(payload, 1, payloadData, 0, payloadData.length);
            String payloadString = new String(payloadData, StandardCharsets.UTF_8);
            String className = payloadString.split(";")[0];
            Class<Device> deviceClass = (Class<Device>) Class.forName("models.devices." + className);
            payloadString = payloadString.substring(className.length() + 1);
            return DeviceSerializer.deserialize(payloadString, deviceClass);
        } catch (Exception e) {
            LoggingThread.logError(e.getMessage());
        }
        return null;
    }

    public @NotNull String serializeDevice(@NotNull Device device) {
        try {
            return DeviceSerializer.serialize(device);
        } catch (DeviceSerializer.DeviceSerializationException e) {
            LoggingThread.logError(e.getMessage());
        }
        return "";
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
