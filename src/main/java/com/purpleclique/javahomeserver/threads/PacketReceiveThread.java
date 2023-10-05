package com.purpleclique.javahomeserver.threads;

import com.purpleclique.javahomeserver.utils.NetworkManager;
import com.purpleclique.javahomeserver.utils.SharedState;
import com.purpleclique.javahomeserver.utils.SignalConverter;

import java.net.DatagramPacket;

public class PacketReceiveThread implements Runnable {

    public void run() {
        while (true) {
            DatagramPacket packet = NetworkManager.getInstance().receivePacket();
            SignalConverter.InputSignal inputSignal = SignalConverter.packetToSignal(packet);
            if (inputSignal.getDeviceInputSignal() != null) {
                SharedState.deviceInputSignals.add(inputSignal.getDeviceInputSignal());
            }
        }
    }

}
