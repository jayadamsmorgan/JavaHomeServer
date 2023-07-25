package threads;

import models.signals.device.io.DeviceInputSignal;
import utils.DeviceManager;
import utils.SharedState;

public class DeviceInputThread implements Runnable {

    public void run() {
        while (true) {
            try {
                DeviceInputSignal signal = SharedState.deviceInputSignals.take();
                DeviceManager.getInstance().parseDeviceInputSignal(signal);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
