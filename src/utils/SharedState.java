package utils;

import models.devices.Device;
import models.signals.controllingdevice.io.ControllingDeviceInputGetSignal;
import models.signals.controllingdevice.io.ControllingDeviceInputOutSignal;
import models.signals.device.io.DeviceInputSignal;
import models.signals.device.io.DeviceOutputSignal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class SharedState {

    public static volatile Set<Device> devices = Collections.synchronizedSet(new HashSet<>());

    public static volatile LinkedBlockingQueue<DeviceOutputSignal> deviceOutputSignals = new LinkedBlockingQueue<>();
    public static volatile LinkedBlockingQueue<DeviceInputSignal> deviceInputSignals = new LinkedBlockingQueue<>();
    public static volatile LinkedBlockingQueue<ControllingDeviceInputOutSignal> controllingDeviceInputOutSignals
            = new LinkedBlockingQueue<>();
    public static volatile LinkedBlockingQueue<ControllingDeviceInputGetSignal> controllingDeviceInputGetSignals
            = new LinkedBlockingQueue<>();
}
