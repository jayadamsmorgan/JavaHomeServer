package models.devices.sensors;

import models.devices.BasicDevice;

public class Sensor extends BasicDevice {

    protected Sensor() {
        super();
    }

    public Sensor(String ipAddress) {
        super(ipAddress);
    }

    public void requestSensorData() {

    }

    public String toString() {
        return "Sensor{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", on=" + on +
                '}';
    }
}
