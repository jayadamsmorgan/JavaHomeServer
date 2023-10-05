package com.purpleclique.javahomeserver.models.devices;

public interface Device {

    int getId();

    void setName(String name);

    void setLocation(String location);

    void setId(int id);

    boolean isOn();

    void setIsOn(boolean isOn);

    String getIpAddress();

    void setIpAddress(String ipAddress);

    String getName();

    String getData();

    void setData(String data);

    String getLocation();

    void save();

}
