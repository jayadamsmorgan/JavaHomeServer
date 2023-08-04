package threads;

import models.signals.device.io.DeviceInputSignal;
import models.signals.device.io.DeviceOutputSignal;
import utils.SharedState;
import utils.SignalConverter;

public class DeviceInputThread implements Runnable {

    public void run() {
        while (true) {
            try {
                DeviceInputSignal signal = SharedState.deviceInputSignals.take();
                if (signal.deviceToBeUpdated() == null) {
                    // Saving new Device
                    signal.inputDevice().save();
                    SharedState.devices.add(signal.inputDevice());
                    // Sending issued ID back to the Device
                    DeviceOutputSignal deviceOutputSignal = SignalConverter.deviceOutputSignal(signal.inputDevice());
                    deviceOutputSignal.send();
                } else {
                    // Updating Device
                    SharedState.devices.removeIf(device -> device.getId() == signal.deviceToBeUpdated().getId());
                    SharedState.devices.add(signal.inputDevice());
                    signal.inputDevice().save();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
