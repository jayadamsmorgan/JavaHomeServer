package models.devices.lights;

import models.signals.device.lights.RGBSignal;
import org.jetbrains.annotations.NotNull;

public class RGBStrip extends LightDevice {

    private RGBSignal color;

    public void setColor(@NotNull RGBSignal signal) {
        color = signal;
    }

    public RGBSignal getColor() {
        return color;
    }

}
