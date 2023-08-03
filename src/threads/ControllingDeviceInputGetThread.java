package threads;

import models.signals.controllingdevice.io.ControllingDeviceInputGetSignal;
import models.signals.controllingdevice.io.ControllingDeviceOutputSignal;
import utils.SharedState;
import utils.SignalConverter;

public class ControllingDeviceInputGetThread implements Runnable {

    public void run() {
        while (true) {
            try {
                ControllingDeviceInputGetSignal signal = SharedState.controllingDeviceInputGetSignals.take();
                ControllingDeviceOutputSignal outputSignal =
                        SignalConverter.controllingInputToControllingOutput(signal);
                outputSignal.send();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
