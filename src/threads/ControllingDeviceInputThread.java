package threads;

import models.signals.controllingdevice.io.ControllingDeviceInputSignal;
import models.signals.controllingdevice.io.ControllingDeviceOutputSignal;
import models.signals.controllingdevice.requests.Request;
import models.signals.device.io.DeviceOutputSignal;
import utils.SharedState;
import utils.SignalConverter;

public class ControllingDeviceInputThread implements Runnable {

    public void run() {
        while (true) {
            try {
                ControllingDeviceInputSignal signal = SharedState.controllingDeviceInputSignals.take();
                DeviceOutputSignal deviceOutputSignal = SignalConverter.controllingInputToDeviceOutput(signal);
                if (deviceOutputSignal == null) {
                    continue;
                }
                if (SignalConverter.checkIfControllingDevicePacketIsGoingThrough(signal)) {
                    SharedState.deviceOutputSignals.add(SignalConverter.controllingInputToDeviceOutput(signal));
                    continue;
                }
                Request request = SignalConverter.controllingInputToRequest(signal);
                if (request == null || request.getRequestType() == null) {
                    continue;
                }
                ControllingDeviceOutputSignal outputSignal = SignalConverter.getControllingOutputFromRequest(request);
                if (outputSignal != null) {
                    SharedState.controllingDeviceOutputSignals.add(outputSignal);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
