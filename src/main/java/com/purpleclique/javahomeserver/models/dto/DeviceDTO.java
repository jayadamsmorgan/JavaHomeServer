package com.purpleclique.javahomeserver.models.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.purpleclique.javahomeserver.models.devices.BasicDevice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonDeserialize(using = DeviceDTODeserializer.class)
public class DeviceDTO {

    private String deviceType;
    private BasicDevice device;

}
