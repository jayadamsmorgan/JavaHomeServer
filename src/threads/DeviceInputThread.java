package threads;

import models.signals.device.io.DeviceInputSignal;
import utils.SharedState;

public class DeviceInputThread implements Runnable {

    public void run() {
        while (true) {
            try {
                DeviceInputSignal signal = SharedState.deviceInputSignals.take();
                SharedState.devices.remove(signal.deviceToBeUpdated());
                SharedState.devices.add(signal.inputDevice());
                signal.inputDevice().save();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
