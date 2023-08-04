package utils;

import threads.LoggingThread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkManager {

    private static final int PORT = 8080;
    public static final int RECEIVE_PACKET_MAX_LENGTH = 1024;

    private static NetworkManager instance;
    private final DatagramSocket socket;

    private NetworkManager() {
        try {
            socket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public void sendPacket(String targetAddress, byte[] packet) {
        try (ExecutorService service = Executors.newCachedThreadPool()) {
            service.submit(() -> {
                try {
                    DatagramPacket datagramPacket = new DatagramPacket(packet, packet.length,
                            InetAddress.getByName(targetAddress), PORT);
                    socket.send(datagramPacket);
                    LoggingThread.log("Sent packet to '" + targetAddress + "'.");
                } catch (IOException e) {
                    LoggingThread.logError("Cannot send packet to '" + targetAddress + "': " + e.getMessage());
                }
            });
        }
    }

    public DatagramPacket receivePacket() {
        byte[] buffer = new byte[RECEIVE_PACKET_MAX_LENGTH];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, RECEIVE_PACKET_MAX_LENGTH);
        datagramPacket.setLength(RECEIVE_PACKET_MAX_LENGTH);
        try {
            socket.receive(datagramPacket);
            LoggingThread.log("Received packet from '" + datagramPacket.getAddress().getHostAddress() + "'.");
        } catch (IOException e) {
            LoggingThread.logError("Cannot receive packet: " + e.getMessage());
        }
        return datagramPacket;
    }

}
