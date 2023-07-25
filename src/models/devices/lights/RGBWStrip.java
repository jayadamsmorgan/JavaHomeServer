package models.devices.lights;

import models.signals.device.lights.RGBWSignal;
import org.jetbrains.annotations.NotNull;

public class RGBWStrip extends LightDevice {

    protected RGBWStrip() {
        super();
    }

    public RGBWStrip(String ipAddress) {
        super(ipAddress);
    }

    public void setColor(@NotNull RGBWSignal signal) {

    }

    public String toString() {
        return "RGBWStrip{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", on=" + on +
                '}';
    }

}
