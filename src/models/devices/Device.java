package models.devices;

public interface Device {

    String getId();

    void setName(String name);

    void setLocation(String location);

    boolean isOn();

    void turnOn();

    void turnOff();

    String getIpAddress();

    void setIpAddress(String ipAddress);

    void save();

}
