package com.purpleclique.javahomeserver.models.devices;

public interface Device {

    String getId();

    void setName(String name);

    void setLocation(String location);

    void setId(String id);

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
