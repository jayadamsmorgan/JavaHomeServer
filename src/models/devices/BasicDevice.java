package models.devices;

import utils.DeviceManager;

import java.util.concurrent.atomic.AtomicBoolean;

public class BasicDevice implements Device {

    protected int id;
    protected String name;
    protected String ipAddress;
    protected String location;
    protected String data;
    protected boolean on;
    protected volatile AtomicBoolean lock;

    public BasicDevice() {
        lock = new AtomicBoolean(false);
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isOn() {
        return on;
    }

    public void setIsOn(boolean isOn) {
        this.on = isOn;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getLocation() {
        return location;
    }

    public void save() {
        while (lock.get()) {
            Thread.yield();
        }
        lock.set(true);
        DeviceManager.getInstance().saveDevice(this);
        lock.set(false);
    }

    public String toString() {
        return this.getClass().getCanonicalName() + "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", location='" + location + '\'' +
                ", data='" + data + '\'' +
                ", on=" + on +
                '}';
    }
}
