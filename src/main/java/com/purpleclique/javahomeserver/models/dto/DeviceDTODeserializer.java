package com.purpleclique.javahomeserver.models.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.purpleclique.javahomeserver.models.devices.BasicDevice;
import com.purpleclique.javahomeserver.models.devices.LightDevice;
import com.purpleclique.javahomeserver.models.devices.RGBLightDevice;
import lombok.NonNull;

import java.io.IOException;

public class DeviceDTODeserializer extends JsonDeserializer<DeviceDTO> {

    @Override
    public DeviceDTO deserialize(@NonNull JsonParser p, DeserializationContext context) throws IOException {
        ObjectCodec oc = p.getCodec();
        JsonNode node = oc.readTree(p);

        String deviceType = node.get("deviceType").asText();

        BasicDevice device;
        JsonNode deviceNode = node.get("device");
        ObjectMapper mapper = (ObjectMapper) p.getCodec();

        // TODO: register new Device classes here
        switch (deviceType) {
            case "RGBLightDevice", "lights.RGBLightDevice" ->
                    device = mapper.treeToValue(deviceNode, RGBLightDevice.class);
            case "LightDevice", "lights.LightDevice" ->
                    device = mapper.treeToValue(deviceNode, LightDevice.class);
            default ->
                    device = mapper.treeToValue(deviceNode, BasicDevice.class);
        }

        DeviceDTO wrapper = new DeviceDTO();
        wrapper.setDeviceType(deviceType);
        wrapper.setDevice(device);
        return wrapper;
    }
}
