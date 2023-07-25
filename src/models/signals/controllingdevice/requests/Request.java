package models.signals.controllingdevice.requests;

import models.devices.ControllingDevice;
import models.devices.Device;

public interface Request {

    Device getTargetDevice();

    void setTargetDevice(Device targetDevice);

    ControllingDevice getSourceControllingDevice();

    RequestType getRequestType();

    void setRequestType(RequestType requestType);

    enum RequestType {
        REQUEST_TYPE_GET_ALL_DEVICES_INFO,
        REQUEST_TYPE_GET_DEVICE_INFO,
        REQUEST_TYPE_GET_SENSOR_DATA,
    }

    byte[] getPayload();

    void setPayload(byte[] payload);

}
