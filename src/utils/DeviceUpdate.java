package utils;

import models.devices.Device;
import org.jetbrains.annotations.NotNull;
import threads.LoggingThread;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.random.RandomGenerator;

public class DeviceUpdate {

    private static final int DEFAULT_OTA_UPDATE_PORT = 3232;
    private static final String DEFAULT_OTA_PASSWORD = "admin";

    public static void sendUpdate(@NotNull File updateFile, @NotNull Device targetDevice) {
        sendUpdate(updateFile, targetDevice, DEFAULT_OTA_UPDATE_PORT, DEFAULT_OTA_PASSWORD);
    }

    public static void sendUpdate(@NotNull File updateFile,
                                  @NotNull Device targetDevice,
                                  int otaUpdatePort,
                                  @NotNull String otaPassword) {
        if (!updateFile.exists()) {
            LoggingThread.logError("Cannot start Device Update: Update file does not exist.");
            return;
        }
        String deviceUpdateString = "Device Update: Device ID '" + targetDevice.getId() + "': ";
        int localPort = RandomGenerator.getDefault().nextInt(10_000, 60_000);
        LoggingThread.log(deviceUpdateString + "Starting Update on port '" + localPort + "'.");
        try (ServerSocket serverSocket = new ServerSocket(localPort)) {
            long fileLength = updateFile.length();
            LoggingThread.log(deviceUpdateString + "Upload size is '" + fileLength + "' bytes.");
            String fileMD5 = getFileMD5Checksum(updateFile) ;
            String data = null;
            LoggingThread.log(deviceUpdateString + "Waiting for Device response...");
            for (int i = 0; i < 5; i++) {
                try (DatagramSocket udpSocket = new DatagramSocket()) {
                    String message = "0 " + localPort + " " + fileLength + " " + fileMD5 + "\n";
                    byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                    udpSocket.send(new DatagramPacket(messageBytes, messageBytes.length,
                            InetAddress.getByName(targetDevice.getIpAddress()), otaUpdatePort));
                    DatagramPacket incomingPacket = new DatagramPacket(new byte[37], 37,
                            InetAddress.getByName(targetDevice.getIpAddress()), 3232);
                    try {
                        udpSocket.receive(incomingPacket);
                        data = new String(incomingPacket.getData(), StandardCharsets.UTF_8);
                        if (!data.startsWith("OK")) {
                            if (data.startsWith("AUTH")) {
                                // DO NOT ask me about the following...
                                // That is the AUTH algorithm for Arduino BasicOTA... ;)
                                String nonce = data.split(" ")[1];
                                String cNonceString = updateFile.getAbsolutePath()
                                        + fileLength + fileMD5 + targetDevice.getIpAddress();
                                String cNonce = getStringMD5Hash(cNonceString);
                                String passwordMD5 = getStringMD5Hash(otaPassword);
                                String resultString = passwordMD5 + ":" + nonce + ":" + cNonce;
                                String result = getStringMD5Hash(resultString);
                                LoggingThread.log(deviceUpdateString + "Authenticating Device Update...");
                                message = "200" + " " + cNonce + " " +  result + "\n";
                                messageBytes = message.getBytes(StandardCharsets.UTF_8);
                                udpSocket.send(new DatagramPacket(messageBytes, messageBytes.length,
                                        InetAddress.getByName(targetDevice.getIpAddress()), 3232));
                                incomingPacket = new DatagramPacket(new byte[32], 32);
                                udpSocket.receive(incomingPacket);
                                data = new String(incomingPacket.getData(), StandardCharsets.UTF_8);
                                if (!data.startsWith("OK")) {
                                    udpSocket.close();
                                    throw new Exception("Authentication Failed: " + data);
                                }
                            } else {
                                udpSocket.close();
                                throw new Exception("Bad response: " + data);
                            }
                        }
                        udpSocket.close();
                        Socket connection = serverSocket.accept();
                        LoggingThread.log(deviceUpdateString + "Starting uploading Update file...");
                        FileInputStream fileInputStream = new FileInputStream(updateFile);
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                        OutputStream outputStream = connection.getOutputStream();
                        int bytesRead;
                        byte[] buffer = new byte[1024];
                        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            outputStream.flush();
                        }
                        LoggingThread.log(deviceUpdateString + "Device Successfully updated.");
                    } catch (IOException ignored) { }
                }
            }
            if (data == null) {
                serverSocket.close();
                throw new Exception("No response from Device.");
            }
        } catch (Exception e) {
            LoggingThread.logError(deviceUpdateString + "Cannot update Device: " + e.getMessage() + ".");
        }
    }

    private static @NotNull String getFileMD5Checksum(@NotNull File file) throws Exception {
        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        try (InputStream fis = new FileInputStream(file)) {
            int numRead;
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
        }

        byte[] b = complete.digest();
        StringBuilder result = new StringBuilder();
        for (byte value : b) {
            result.append(Integer.toString((value & 0xFF) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    public static @NotNull String getStringMD5Hash(@NotNull String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(input.getBytes(StandardCharsets.UTF_8));
        byte[] bytes = digest.digest();
        StringBuilder buffer = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                buffer.append('0');
            }
            buffer.append(hex);
        }
        return buffer.toString();
    }

}

