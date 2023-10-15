package com.purpleclique.javahomeserver.threads;

import com.purpleclique.javahomeserver.models.io.DeviceInputSignal;
import com.purpleclique.javahomeserver.models.io.DeviceOutputSignal;
import com.purpleclique.javahomeserver.utils.SignalConverter;

import java.util.concurrent.LinkedBlockingQueue;

public class DeviceInputThread implements Runnable {

    public static volatile LinkedBlockingQueue<DeviceInputSignal> deviceInputSignals = new LinkedBlockingQueue<>();

    public void run() {
        while (true) {
            try {
                DeviceInputSignal signal = deviceInputSignals.take();
                if (signal.deviceToBeUpdated() == null) {
                    // Saving new Device
                    signal.inputDevice().save();
                    // Sending issued ID back to the Device
                    DeviceOutputSignal deviceOutputSignal = SignalConverter.deviceOutputSignal(signal.inputDevice());
                    deviceOutputSignal.send();
                } else {
                    // Updating Device
                    signal.inputDevice().save();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
