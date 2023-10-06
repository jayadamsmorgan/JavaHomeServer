package com.purpleclique.javahomeserver.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.purpleclique.javahomeserver.models.devices.Device;
import com.purpleclique.javahomeserver.models.dto.DeviceDTO;
import com.purpleclique.javahomeserver.threads.LoggingThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public @Nullable DeviceDTO deserializeFromPayload(byte @NotNull [] payload) {
        try {
            String payloadString = new String(payload, StandardCharsets.UTF_8).substring(1);
            return DeviceSerializer.deserialize(payloadString);
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

        public static DeviceDTO deserialize(String serializedDevice) throws DeviceSerializationException {
            try {
                return mapper.readValue(serializedDevice, DeviceDTO.class);
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
