package models.devices;

import threads.LoggingThread;
import utils.DeviceManager;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class BasicDevice implements Device {

    protected String id;
    protected String name;
    protected String ipAddress;
    protected String location;
    protected boolean on;
    protected volatile AtomicBoolean lock;

    protected BasicDevice() {
        lock = new AtomicBoolean(false);
        // Needed for Jackson's JSON Serializing
    }

    protected BasicDevice(String ipAddress) {
        lock = new AtomicBoolean(false);
        this.ipAddress = ipAddress;
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isOn() {
        return on;
    }

    public void turnOn() {
        this.on = true;
        save();
    }

    public void turnOff() {
        this.on = false;
        save();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void save() {
        while (lock.get()) {
            Thread.yield();
        }
        lock.set(true);
        try {
            DeviceManager.getInstance().saveDevice(this);
        } catch (DeviceManager.DeviceNotSavedException e) {
            LoggingThread.logError("Cannot save Device with ID '" + id + "': " + e.getMessage());
        }
        lock.set(false);
    }

}
