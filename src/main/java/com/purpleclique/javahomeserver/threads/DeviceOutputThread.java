package com.purpleclique.javahomeserver.threads;

import com.purpleclique.javahomeserver.models.io.DeviceOutputSignal;

import java.util.concurrent.LinkedBlockingQueue;

public class DeviceOutputThread implements Runnable {

    public static volatile LinkedBlockingQueue<DeviceOutputSignal> deviceOutputSignals = new LinkedBlockingQueue<>();

    public void run() {
        while (true) {
            try {
                DeviceOutputSignal signal = deviceOutputSignals.take();
                signal.send();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
