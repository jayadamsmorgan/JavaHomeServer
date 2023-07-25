package threads;

import models.signals.device.io.DeviceOutputSignal;
import utils.SharedState;

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
