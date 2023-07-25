package models.devices.lights;

import models.devices.BasicDevice;
import models.signals.device.lights.BrightnessSignal;
import org.jetbrains.annotations.NotNull;
import threads.LoggingThread;
import utils.DeviceManager;
import utils.SharedState;

public class LightDevice extends BasicDevice {

    protected LightDevice() {
        super();
    }

    public LightDevice(String ipAddress) {
        super(ipAddress);
    }

    public void setBrightness(@NotNull BrightnessSignal signal) {
        if (signal.getBrightness() == BrightnessSignal.MIN_BRIGHTNESS) {
            turnOff();
        } else if (!isOn()) {
            turnOn();
        }
    }

    public void save() {
        try {
            DeviceManager.getInstance().saveDevice(this);
        } catch (DeviceManager.DeviceNotSavedException e) {
            LoggingThread.logError("Cannot save device with IP: '" + ipAddress + "': " + e.getMessage());
        }
        SharedState.devices.add(this);
    }

    public String toString() {
        return "LightDevice{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", on=" + on +
                '}';
    }
}
