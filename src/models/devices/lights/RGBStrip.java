package models.devices.lights;

import models.signals.device.lights.RGBSignal;
import org.jetbrains.annotations.NotNull;

public class RGBStrip extends LightDevice {

    private RGBSignal color;

    protected RGBStrip() {
        super();
    }

    public RGBStrip(String ipAddress) {
        super(ipAddress);
    }

    public void setColor(@NotNull RGBSignal signal) {
        color = signal;
    }

    public RGBSignal getColor() {
        return color;
    }

    public String toString() {
        return "RGBStrip{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", on=" + on +
                '}';
    }
}
