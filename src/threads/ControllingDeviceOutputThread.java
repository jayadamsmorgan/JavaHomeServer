package threads;

import models.signals.controllingdevice.io.ControllingDeviceOutputSignal;
import utils.SharedState;

public class ControllingDeviceOutputThread implements Runnable {

    public void run() {
        while (true) {
            try {
                ControllingDeviceOutputSignal signal = SharedState.controllingDeviceOutputSignals.take();
                signal.send();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
