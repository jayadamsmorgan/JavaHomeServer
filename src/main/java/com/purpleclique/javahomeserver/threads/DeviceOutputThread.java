package com.purpleclique.javahomeserver.threads;

import com.purpleclique.javahomeserver.models.io.DeviceOutputSignal;
import com.purpleclique.javahomeserver.utils.SharedState;

public class DeviceOutputThread implements Runnable {

    public void run() {
        while (true) {
            try {
                DeviceOutputSignal signal = SharedState.deviceOutputSignals.take();
                signal.send();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
