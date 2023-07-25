package models.signals.controllingdevice.requests;

import models.devices.ControllingDevice;
import models.devices.Device;

public class BasicRequest implements Request {

    private Device targetDevice;
    private final ControllingDevice sourceControllingDevice;
    private RequestType requestType;
    private byte[] payload;

    public BasicRequest(ControllingDevice sourceControllingDevice) {
        this.sourceControllingDevice = sourceControllingDevice;
    }

    public Device getTargetDevice() {
        return targetDevice;
    }

    public ControllingDevice getSourceControllingDevice() {
        return sourceControllingDevice;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public void setTargetDevice(Device targetDevice) {
        this.targetDevice = targetDevice;
    }
}
