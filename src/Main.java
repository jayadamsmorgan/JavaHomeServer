import threads.*;
import utils.DeviceManager;
import utils.SharedState;

public class Main {

    private static final String version = "0.23";

    private static boolean isVerboseEnabled = false;
    private static boolean isLoggingEnabled = false;

    public static void main(String[] args) {

        welcomeLines();

        argumentsInit(args);

        loadDevices();

        threadsInit();

        System.out.println("Home server started.");
    }

    private static void welcomeLines() {
        String welcomeString = """
                    
                    __  ______  __  _________   _____ __________ _    ____________\s
                   / / / / __ \\/  |/  / ____/  / ___// ____/ __ \\ |  / / ____/ __ \\
                  / /_/ / / / / /|_/ / __/     \\__ \\/ __/ / /_/ / | / / __/ / /_/ /
                 / __  / /_/ / /  / / /___    ___/ / /___/ _, _/| |/ / /___/ _, _/\s
                /_/ /_/\\____/_/  /_/_____/   /____/_____/_/ |_| |___/_____/_/ |_| \s
                                                                                  \s
                """;
        System.out.println(LoggingThread.ANSI_PURPLE + welcomeString + LoggingThread.ANSI_RESET);
        System.out.println(LoggingThread.ANSI_GREEN + "Java Smart Home Server v" + version + LoggingThread.ANSI_RESET);
    }

    private static void argumentsInit(String[] args) {
        if (args == null) {
            return;
        }
        for (String arg : args) {
            switch (arg) {
                case "-v", "-verbose" -> {
                    isVerboseEnabled = true;
                    System.out.println("Verbose logging enabled.");
                }
                case "-l", "-log", "logging" -> {
                    isLoggingEnabled = true;
                    System.out.println("File logging enabled.");
                }
            }
        }
    }

    private static void loadDevices() {
        System.out.print("Loading Devices... ");
        try {
            SharedState.devices.addAll(DeviceManager.getInstance().loadDevices());
        } catch (DeviceManager.DeviceNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (SharedState.devices.isEmpty()) {
            System.out.println("No devices available yet.");
        } else {
            System.out.println("Done");
        }
    }

    private static void threadsInit() {
        System.out.print("Initializing threads");

        LoggingThread loggingThreadRunnable = new LoggingThread(isVerboseEnabled, isLoggingEnabled);
        Thread loggingThread = new Thread(loggingThreadRunnable);
        loggingThread.start();
        UserInputThread userInputThreadRunnable = new UserInputThread();
        Thread userInputThread = new Thread(userInputThreadRunnable);
        userInputThread.start();
        PacketReceiveThread packetReceiveThreadRunnable = new PacketReceiveThread();
        Thread packetReceiveThread = new Thread(packetReceiveThreadRunnable);
        packetReceiveThread.start();
        System.out.print(".");

        ControllingDeviceOutputThread controllingDeviceOutputThreadRunnable = new ControllingDeviceOutputThread();
        Thread controllingDeviceOutputThread = new Thread(controllingDeviceOutputThreadRunnable);
        controllingDeviceOutputThread.start();
        ControllingDeviceInputThread controllingDeviceInputThreadRunnable = new ControllingDeviceInputThread();
        Thread controllingDeviceInputThread = new Thread(controllingDeviceInputThreadRunnable);
        controllingDeviceInputThread.start();
        System.out.print(".");

        DeviceInputThread deviceInputThreadRunnable = new DeviceInputThread();
        Thread deviceInputThread = new Thread(deviceInputThreadRunnable);
        deviceInputThread.start();
        DeviceOutputThread deviceOutputThreadRunnable = new DeviceOutputThread();
        Thread deviceControllerThread = new Thread(deviceOutputThreadRunnable);
        deviceControllerThread.start();
        System.out.print(".");

        System.out.println(" Done.");
    }

}