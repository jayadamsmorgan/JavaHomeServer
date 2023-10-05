package com.purpleclique.javahomeserver.utils;

import com.purpleclique.javahomeserver.models.devices.Device;
import com.purpleclique.javahomeserver.models.io.DeviceInputSignal;
import com.purpleclique.javahomeserver.models.io.DeviceOutputSignal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class SharedState {

    public static volatile Set<Device> devices = Collections.synchronizedSet(new HashSet<>());

    public static volatile LinkedBlockingQueue<DeviceOutputSignal> deviceOutputSignals = new LinkedBlockingQueue<>();
    public static volatile LinkedBlockingQueue<DeviceInputSignal> deviceInputSignals = new LinkedBlockingQueue<>();
}
