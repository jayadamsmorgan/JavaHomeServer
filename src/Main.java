import threads.*;
import utils.DBUtil;
import utils.SharedState;

public class Main {

    private static final String version = "0.5";

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
                default -> {
                    System.err.println("Wrong argument: " + arg);
                    System.exit(0);
                }
            }
        }
    }

    private static void loadDevices() {
        System.out.print("Loading Devices... ");
        SharedState.devices.addAll(DBUtil.getInstance().loadDevices());
        if (SharedState.devices.isEmpty()) {
            System.out.println("No devices available yet.");
        } else {
            System.out.println("Done. Device count: " + SharedState.devices.size() + ".");
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

        DeviceInputThread deviceInputThreadRunnable = new DeviceInputThread();
        Thread deviceInputThread = new Thread(deviceInputThreadRunnable);
        deviceInputThread.start();
        DeviceOutputThread deviceOutputThreadRunnable = new DeviceOutputThread();
        Thread deviceOutputThread = new Thread(deviceOutputThreadRunnable);
        deviceOutputThread.start();
        System.out.print(".");

        ControllingDeviceInputGetThread controllingDeviceInputGetThreadRunnable = new ControllingDeviceInputGetThread();
        Thread controllingDeviceInputGetThread = new Thread(controllingDeviceInputGetThreadRunnable);
        controllingDeviceInputGetThread.start();
        ControllingDeviceInputOutThread controllingDeviceInputOutThreadRunnable = new ControllingDeviceInputOutThread();
        Thread controllingDeviceInputOutThread = new Thread(controllingDeviceInputOutThreadRunnable);
        controllingDeviceInputOutThread.start();
        System.out.print(".");

        System.out.println(" Done.");
    }

}