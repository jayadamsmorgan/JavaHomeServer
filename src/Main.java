import org.apache.commons.cli.*;
import org.jetbrains.annotations.NotNull;
import sun.misc.Signal;
import threads.*;
import utils.DBUtil;
import utils.SharedState;

public class Main {

    private static final String version = "0.69";

    private static boolean isConsoleLoggingEnabled = false;
    private static boolean isFileLoggingEnabled = false;
    private static int fileLogLevel = LoggingThread.LOG_LEVEL_ERROR;
    private static int consoleLogLevel = LoggingThread.LOG_LEVEL_ERROR;

    public static void main(String[] args) {

        welcomeLines();

        parseArguments(args);

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

    private static void parseArguments(String[] args) {
        Option consoleLogging = Option.builder("c")
                .hasArg()
                .optionalArg(true)
                .longOpt("console-log")
                .desc("""
                        Enable console logging.
                        Arg: Log level:
                        0 - NONE
                        1 - ERROR (DEFAULT)
                        2 - WARNING
                        3 - VERBOSE
                        """)
                .required(false)
                .build();
        Option fileLogging = Option.builder("f")
                .hasArg()
                .optionalArg(true)
                .longOpt("file-log")
                .desc("""
                        Enable file logging.
                        Generates 'log.txt'.
                        Arg: Log level:
                        0 - NONE
                        1 - ERROR (DEFAULT)
                        2 - WARNING
                        3 - VERBOSE
                        """)
                .required(false)
                .build();
        Option help = Option.builder("h")
                .hasArg(false)
                .longOpt("help")
                .desc("Display help menu.")
                .required(false)
                .build();

        Options options = new Options();
        options.addOption(consoleLogging);
        options.addOption(fileLogging);
        options.addOption(help);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                helpFormatter.printHelp("javahomeserver.jar [-c [LOGLEVEL=1]] [-f [LOGLEVEL=1]]", options);
                System.exit(0);
            }

            if (cmd.hasOption("c")) {
                System.out.println("Console logging enabled.");
                isConsoleLoggingEnabled = true;
                String logLevelString = cmd.getOptionValue("c");
                System.out.print("Console output logging level: ");
                if (logLevelString != null) {
                    switch (logLevelString) {
                        case "0", "NONE" -> {
                            consoleLogLevel = LoggingThread.LOG_LEVEL_NONE;
                            System.out.println(LoggingThread.ANSI_PURPLE + "NONE" + LoggingThread.ANSI_RESET + ".");
                        }
                        case "2", "WARNING" -> {
                            consoleLogLevel = LoggingThread.LOG_LEVEL_WARNING;
                            System.out.println(LoggingThread.ANSI_YELLOW + "WARNING" + LoggingThread.ANSI_RESET + ".");
                        }
                        case "3", "VERBOSE" -> {
                            consoleLogLevel = LoggingThread.LOG_LEVEL_VERBOSE;
                            System.out.println(LoggingThread.ANSI_GREEN + "VERBOSE" + LoggingThread.ANSI_RESET + ".");
                        }
                        default -> {
                            consoleLogLevel = LoggingThread.LOG_LEVEL_ERROR;
                            System.out.println(LoggingThread.ANSI_RED + "ERROR" + LoggingThread.ANSI_RESET + ".");
                        }
                    }
                } else {
                    consoleLogLevel = LoggingThread.LOG_LEVEL_ERROR;
                    System.out.println(LoggingThread.ANSI_RED + "ERROR" + LoggingThread.ANSI_RESET + ".");
                }
            }

            if (cmd.hasOption("f")) {
                System.out.println("File logging enabled.");
                isFileLoggingEnabled = true;
                String logLevelString = cmd.getOptionValue("f");
                System.out.print("File output logging level: ");
                if (logLevelString != null) {
                    switch (logLevelString) {
                        case "0", "NONE" -> {
                            fileLogLevel = LoggingThread.LOG_LEVEL_NONE;
                            System.out.println(LoggingThread.ANSI_PURPLE + "NONE" + LoggingThread.ANSI_RESET + ".");
                        }
                        case "2", "WARNING" -> {
                            fileLogLevel = LoggingThread.LOG_LEVEL_WARNING;
                            System.out.println(LoggingThread.ANSI_YELLOW + "WARNING" + LoggingThread.ANSI_RESET + ".");
                        }
                        case "3", "VERBOSE" -> {
                            fileLogLevel = LoggingThread.LOG_LEVEL_VERBOSE;
                            System.out.println(LoggingThread.ANSI_GREEN + "VERBOSE" + LoggingThread.ANSI_RESET + ".");
                        }
                        default -> {
                            fileLogLevel = LoggingThread.LOG_LEVEL_ERROR;
                            System.out.println(LoggingThread.ANSI_RED + "ERROR" + LoggingThread.ANSI_RESET + ".");
                        }
                    }
                } else {
                    fileLogLevel = LoggingThread.LOG_LEVEL_ERROR;
                    System.out.println(LoggingThread.ANSI_RED + "ERROR" + LoggingThread.ANSI_RESET + ".");
                }
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            helpFormatter.printHelp("javahomeserver.jar [-c [LOGLEVEL=1]] [-f [LOGLEVEL=1]]", options);
            System.exit(0);
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

        startNewThread(new LoggingThread(isConsoleLoggingEnabled, isFileLoggingEnabled, consoleLogLevel, fileLogLevel));
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