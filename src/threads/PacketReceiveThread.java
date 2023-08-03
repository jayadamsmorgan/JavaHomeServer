package threads;

import utils.NetworkManager;
import utils.SharedState;
import utils.SignalConverter;

import java.net.DatagramPacket;

public class PacketReceiveThread implements Runnable {

    public void run() {
        while (true) {
            DatagramPacket packet = NetworkManager.getInstance().receivePacket();
            SignalConverter.InputSignal inputSignal = SignalConverter.packetToSignal(packet);
            if (inputSignal.getDeviceInputSignal() != null) {
                SharedState.deviceInputSignals.add(inputSignal.getDeviceInputSignal());
            }
            if (inputSignal.getControllingDeviceInputOutSignal() != null) {
                SharedState.controllingDeviceInputOutSignals.add(inputSignal.getControllingDeviceInputOutSignal());
            }
            if (inputSignal.getControllingDeviceInputGetSignal() != null) {
                SharedState.controllingDeviceInputGetSignals.add(inputSignal.getControllingDeviceInputGetSignal());
            }
        }
    }

}
