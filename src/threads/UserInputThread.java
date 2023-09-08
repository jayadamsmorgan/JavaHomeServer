package threads;

import models.devices.Device;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.Nullable;
import sun.misc.Signal;
import utils.SharedState;
import utils.SignalConverter;

import java.util.Scanner;

public class UserInputThread implements Runnable {

    private final Scanner scanner;

    private final Options serverOptions = new Options();
    private final HelpFormatter serverHelpFormatter = new HelpFormatter();

    private final Options deviceOptions = new Options();
    private final HelpFormatter deviceHelpFormatter = new HelpFormatter();

    public UserInputThread() {
        scanner = new Scanner(System.in);
        setupServerOptions();
        setupDeviceOptions();
    }

    private void setupServerOptions() {
        Option restartOption = Option.builder("r")
                .longOpt("restart")
                .hasArg(false)
                .required(false)
                .desc("Restart Smart Home Server.")
                .build();
        Option clearDatabaseOption = Option.builder()
                .longOpt("clear-db")
                .hasArg(false)
                .required(false)
                .desc("Clear device database.")
                .build();
        Option clearLogOption = Option.builder()
                .longOpt("clear-log")
                .hasArg(false)
                .required(false)
                .desc("Clear 'log.txt'.")
                .build();
        Option shutdownOption = Option.builder("s")
                .longOpt("shutdown")
                .hasArg(false)
                .required(false)
                .desc("Shutdown Smart Home Server.")
                .build();
        Option helpOption = Option.builder("h")
                .longOpt("help")
                .hasArg(false)
                .required(false)
                .desc("Display 'server' help menu.")
                .build();

        serverOptions.addOption(restartOption);
        serverOptions.addOption(clearDatabaseOption);
        serverOptions.addOption(clearLogOption);
        serverOptions.addOption(shutdownOption);
        serverOptions.addOption(helpOption);

    }

    private void setupDeviceOptions() {
        Option listOption = Option.builder("l")
                .longOpt("list")
                .hasArg(false)
                .required(false)
                .desc("List all devices.")
                .build();
        Option helpOption = Option.builder("h")
                .longOpt("help")
                .hasArg(false)
                .required(false)
                .desc("Display 'device' help menu.")
                .build();

        Option ipOption = Option.builder("ip")
                .hasArg()
                .required(false)
                .desc("Set target device by IP.")
                .build();
        Option idOption = Option.builder("id")
                .hasArg()
                .required(false)
                .desc("Set target device by ID.")
                .build();
        Option allOption = Option.builder("a")
                .hasArg(false)
                .required(false)
                .desc("Set all Devices as target.")
                .build();

        Option changeNameOption = Option.builder("nm")
                .longOpt("name")
                .hasArg()
                .required(false)
                .desc("Change target Device name.")
                .build();
        Option changeLocationOption = Option.builder("loc")
                .longOpt("location")
                .hasArg()
                .required(false)
                .desc("Change target Device location")
                .build();
        Option turnDeviceOnOption = Option.builder("on")
                .hasArg(false)
                .required(false)
                .desc("Turn target Device(s) on.")
                .build();
        Option turnDeviceOffOption = Option.builder("off")
                .hasArg(false)
                .required(false)
                .desc("Turn target Device(s) off.")
                .build();

        OptionGroup destinationOptionGroup = new OptionGroup();
        destinationOptionGroup.addOption(ipOption);
        destinationOptionGroup.addOption(idOption);
        destinationOptionGroup.addOption(allOption);

        OptionGroup actionOptionGroup = new OptionGroup();
        actionOptionGroup.addOption(changeNameOption);
        actionOptionGroup.addOption(changeLocationOption);
        actionOptionGroup.addOption(turnDeviceOnOption);
        actionOptionGroup.addOption(turnDeviceOffOption);

        deviceOptions.addOptionGroup(destinationOptionGroup);
        deviceOptions.addOptionGroup(actionOptionGroup);
        deviceOptions.addOption(listOption);
        deviceOptions.addOption(helpOption);
    }

    private void parseServerArguments(String input) {
        if (input == null) {
            serverHelpFormatter.printHelp("server [option]", serverOptions);
            return;
        }
        String[] args = input.split(" ");
        if (args.length == 0) {
            serverHelpFormatter.printHelp("server [option]", serverOptions);
            return;
        }
        try {
            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine cmd = commandLineParser.parse(serverOptions, args);

            if (cmd.hasOption("h")) {
                serverHelpFormatter.printHelp("server [option]", serverOptions);
                return;
            }

            if (cmd.hasOption("r")) {
                // TODO: 9/7/23 server restart realization
                LoggingThread.log("Console: Restarting Smart Home Server.");
                System.out.println("Server restart is not implemented yet.");
                return;
            }

            if (cmd.hasOption("s")) {
                LoggingThread.log("Console: Shutting down Smart Home Server.");
                Signal.raise(new Signal("INT"));
                return;
            }

            if (cmd.hasOption("clear-log")) {
                // TODO: 9/7/23 log clear realization
                // LoggingThread.log("Console: 'log.txt' cleared.");
                System.out.println("Clearing log.txt is not implemented yet.");
                return;
            }

            if (cmd.hasOption("clear-db")) {
                // TODO: 9/7/23 database clear realization
                // LoggingThread.log("Console: Device Database cleared.");
                System.out.println("Clearing database is not implemented yet.");
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            serverHelpFormatter.printHelp("server [option]", serverOptions);
        }
    }

    private void parseDeviceArguments(String input) {
        if (input == null) {
            deviceHelpFormatter.printHelp("device [ -ip IP | -id ID | -a ] [ ACTION ]", deviceOptions);
            return;
        }
        String[] args = input.split(" ");
        if (args.length == 0) {
            deviceHelpFormatter.printHelp("device [ -ip IP | -id ID | -a ] [ ACTION ]", deviceOptions);
            return;
        }
        try {
            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine cmd = commandLineParser.parse(deviceOptions, args);

            if (cmd.hasOption("h")) {
                deviceHelpFormatter.printHelp("device [ -ip IP | -id ID | -a ] [ ACTION ]", deviceOptions);
                return;
            }

            if (cmd.hasOption("l")) {
                LoggingThread.log("Console: Accessed a list of devices.");
                if (SharedState.devices.size() == 0) {
                    System.out.println("No devices available.");
                    return;
                }
                for (Device device : SharedState.devices) {
                    System.out.println(device.toString());
                }
                return;
            }
            Device targetDevice = null;
            if (cmd.hasOption("a")) {
                if (SharedState.devices.size() == 0) {
                    System.out.println("No devices available.");
                    return;
                }
                if (cmd.hasOption("on")) {
                    LoggingThread.log("Console: Turning all Devices on.");
                    for (Device device : SharedState.devices) {
                        device.setIsOn(true);
                        SharedState.deviceOutputSignals.add(SignalConverter.deviceOutputSignal(device));
                    }
                    return;
                }
                if (cmd.hasOption("off")) {
                    LoggingThread.log("Console: Turning all Devices off.");
                    for (Device device : SharedState.devices) {
                        device.setIsOn(false);
                        SharedState.deviceOutputSignals.add(SignalConverter.deviceOutputSignal(device));
                    }
                    return;
                }
                if (cmd.hasOption("nm")) {
                    System.out.println("Cannot change all Devices names. Why would you do that?");
                    return;
                }
                if (cmd.hasOption("loc")) {
                    LoggingThread.log("Console: Moving all Devices to location '" + cmd.getOptionValue("loc") + "'.");
                    for (Device device : SharedState.devices) {
                        device.setLocation(cmd.getOptionValue("loc"));
                        SharedState.deviceOutputSignals.add(SignalConverter.deviceOutputSignal(device));
                    }
                    return;
                }
                return;
            } else if (cmd.hasOption("id")) {
                targetDevice = findDeviceById(cmd.getOptionValue("id"));
                if (targetDevice == null) {
                    System.out.println("Cannot find device with ID '" + cmd.getOptionValue("id") + "'.");
                    return;
                }
            } else if (cmd.hasOption("ip")) {
                targetDevice = findDeviceByAddress(cmd.getOptionValue("ip"));
                if (targetDevice == null) {
                    System.out.println("Cannot find device with IP '" + cmd.getOptionValue("ip") + "'.");
                    return;
                }
            }
            if (targetDevice == null) {
                deviceHelpFormatter.printHelp("device [ -ip IP | -id ID | -a ] [ ACTION ]", deviceOptions);
                return;
            }
            if (cmd.hasOption("on")) {
                LoggingThread.log("Console: Turning Device with ID '" + targetDevice.getId() + "' on.");
                targetDevice.setIsOn(true);
                SharedState.deviceOutputSignals.add(SignalConverter.deviceOutputSignal(targetDevice));
                return;
            }
            if (cmd.hasOption("off")) {
                LoggingThread.log("Console: Turning Device with ID '" + targetDevice.getId() + "' off.");
                targetDevice.setIsOn(true);
                SharedState.deviceOutputSignals.add(SignalConverter.deviceOutputSignal(targetDevice));
                return;
            }
            if (cmd.hasOption("nm")) {
                LoggingThread.log("Console: Changing Device name with ID '" + targetDevice.getId()
                                  + "' to '" + cmd.getOptionValue("nm") + "'.");
                targetDevice.setName(cmd.getOptionValue("nm"));
                SharedState.deviceOutputSignals.add(SignalConverter.deviceOutputSignal(targetDevice));
                return;
            }
            if (cmd.hasOption("loc")) {
                LoggingThread.log("Console: Changing Device location with ID '" + cmd.getOptionValue("id")
                                  + "' to '" + cmd.getOptionValue("loc") + "'.");
                targetDevice.setLocation(cmd.getOptionValue("loc"));
                SharedState.deviceOutputSignals.add(SignalConverter.deviceOutputSignal(targetDevice));
                return;
            }

            deviceHelpFormatter.printHelp("device [ -ip IP | -id ID | -a ] [ ACTION ]", deviceOptions);

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            deviceHelpFormatter.printHelp("device [ -ip IP | -id ID | -a ] [ ACTION ]", deviceOptions);
        }
    }

    public void run() {
        while (true) {
            String fullCommand = scanner.nextLine().trim();
            if (fullCommand.equals("")) {
                continue;
            }
            String[] commandAndArgs = fullCommand.split(" ");

            if (commandAndArgs[0].equalsIgnoreCase("device")) {
                if (commandAndArgs.length > 1) {
                    parseDeviceArguments(fullCommand.substring(6));
                } else {
                    deviceHelpFormatter.printHelp("device [ -ip IP | -id ID | -a ] [ ACTION ]", deviceOptions);
                }
                continue;
            }
            if (commandAndArgs[0].equalsIgnoreCase("server")) {
                if (commandAndArgs.length > 1) {
                    parseServerArguments(fullCommand.substring(6));
                } else {
                    serverHelpFormatter.printHelp("server [option]", serverOptions);
                }
                continue;
            }

            if (commandAndArgs[0].equalsIgnoreCase("help") || commandAndArgs[0].equals("?")) {
                serverHelpFormatter.printHelp("server [option]", serverOptions);
                System.out.println();
                deviceHelpFormatter.printHelp("device [ -ip IP | -id ID | -a ] [ ACTION ]", deviceOptions);
                continue;
            }

            System.out.println("Unknown command: " + commandAndArgs[0] + ". Print 'help' or '?' for available commands");
        }
    }

    private @Nullable Device findDeviceById(String id) {
        int intId;
        try {
            intId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return null;
        }
        for (Device device : SharedState.devices) {
            if (device.getId() == intId) {
                return device;
            }
        }
        return null;
    }

    private @Nullable Device findDeviceByAddress(String address) {
        for (Device device : SharedState.devices) {
            if (device.getIpAddress().equalsIgnoreCase(address)) {
                return device;
            }
        }
        return null;
    }

}
