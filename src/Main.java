import org.jetbrains.annotations.NotNull;
import sun.misc.Signal;
import threads.*;
import utils.DBUtil;
import utils.SharedState;

public class Main {

    private static final String version = "0.55";

    private static boolean isVerboseEnabled = false;
    private static boolean isLoggingEnabled = false;

    public static void main(String[] args) {

        welcomeLines();

        argumentsInit(args);

        loadDevices();

        threadsInit();

        LoggingThread.log("Home server started.");

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

        startNewThread(new LoggingThread(isVerboseEnabled, isLoggingEnabled));
        startNewThread(new UserInputThread());
        Thread packetReceiveThread = startNewThread(new PacketReceiveThread());
        System.out.print(".");

        startNewThread(new DeviceInputThread());
        startNewThread(new DeviceOutputThread());
        System.out.print(".");

        startNewThread(new ControllingDeviceInputGetThread());
        startNewThread(new ControllingDeviceInputOutThread());
        System.out.print(".");

        System.out.println(" Done.");

        // Shutdown Signal handling
        Signal.handle(new Signal("INT"), sig -> {
            LoggingThread.logWarning("Shutting down Smart Home Server...");
            packetReceiveThread.interrupt();
            LoggingThread.log("Packet receive thread stopped. Waiting for queues to clear...");
            // It's safe to exit if there is nothing in Signal queues.
            while (!SharedState.deviceOutputSignals.isEmpty()
                    || !SharedState.deviceInputSignals.isEmpty()
                    || !SharedState.controllingDeviceInputGetSignals.isEmpty()
                    || !SharedState.controllingDeviceInputOutSignals.isEmpty()) {
                Thread.yield();
            }
            LoggingThread.log("Queues cleared. Shutdown completed.");
            synchronized (Thread.currentThread()) {
                try {
                    Thread.currentThread().wait(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.exit(0);
        });
    }

    private static @NotNull Thread startNewThread(Runnable runnable) {
        Thread newThread = new Thread(runnable);
        newThread.start();
        return newThread;
    }

}