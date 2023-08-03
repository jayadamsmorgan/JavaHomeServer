package threads;

import models.signals.controllingdevice.io.ControllingDeviceInputOutSignal;
import models.signals.device.io.DeviceOutputSignal;
import utils.SharedState;
import utils.SignalConverter;

public class ControllingDeviceInputOutThread implements Runnable {

    public void run() {
        while (true) {
            try {
                ControllingDeviceInputOutSignal signal = SharedState.controllingDeviceInputOutSignals.take();
                DeviceOutputSignal deviceOutputSignal = SignalConverter.controllingInputToDeviceOutput(signal);
                SharedState.deviceOutputSignals.add(deviceOutputSignal);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
