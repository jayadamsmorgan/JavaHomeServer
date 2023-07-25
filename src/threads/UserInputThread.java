package threads;

import models.devices.Device;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.SharedState;

import java.util.Scanner;

public class UserInputThread implements Runnable {

    Scanner scanner;

    public UserInputThread() {
        scanner = new Scanner(System.in);
    }

    public void run() {
        while (true) {
            String fullCommand = scanner.nextLine().trim().toUpperCase();
            if (fullCommand.equals("")) {
                continue;
            }
            try {
                String commandPrefix = fullCommand.split(" ")[0];
                switch (commandPrefix) {
                    case "SWITCH" -> {
                        String[] commandSuffix = fullCommand.split(" ")[1].split("=");
                        switch (commandSuffix[0]) {
                            case "ALL" -> {
                                switch (commandSuffix[1]) {
                                    case "OFF" -> {
                                        LoggingThread.log("Console: Turning all devices off.");
                                        for (Device device : SharedState.devices) {
                                            device.turnOff();
                                        }
                                    }
                                    case "ON" -> {
                                        LoggingThread.log("Console: Turning all devices on.");
                                        for (Device device : SharedState.devices) {
                                            device.turnOn();
                                        }
                                    }
                                    default -> wrongCommand();
                                }
                            }
                            case "IP" -> {
                                Device device = findDeviceByAddress(commandSuffix[1]);
                                if (device != null) {
                                    switchDeviceState(device);
                                } else {
                                    wrongCommand();
                                }
                            }
                            case "ID" -> {
                                Device device = findDeviceById(commandSuffix[1]);
                                if (device != null) {
                                    switchDeviceState(device);
                                } else {
                                    wrongCommand();
                                }
                            }
                            default -> wrongCommand();
                        }
                    }
                    case "CONFIG", "CFG" -> {
                        String[] params = fullCommand.split(" ");
                        String[] commandSuffix = params[1].split("=");
                        switch (commandSuffix[0]) {
                            case "ID" -> {
                                Device device = findDeviceById(commandSuffix[1]);
                                if (device != null) {
                                    changeDeviceConfig(params, device);
                                } else {
                                    wrongCommand();
                                }
                            }
                            case "IP" -> {
                                Device device = findDeviceByAddress(commandSuffix[1]);
                                if (device != null) {
                                    changeDeviceConfig(params, device);
                                } else {
                                    wrongCommand();
                                }
                            }
                            default -> wrongCommand();
                        }
                    }
                    case "DEVLIST", "LIST" -> {
                        if (SharedState.devices.size() != 0) {
                            for (Device device : SharedState.devices) {
                                System.out.println(device.toString());
                            }
                        } else {
                            System.out.println("No devices available.");
                        }
                    }
                    case "HELP", "?" -> System.out.println("""
                        DEVLIST - lists all connected devices
                        SWITCH - turns on/off device
                        """);
                    default -> wrongCommand();
                }
            } catch (IndexOutOfBoundsException e) {
                wrongCommand();
            }
        }
    }

    private void switchDeviceState(@NotNull Device device) {
        if (device.isOn()) {
            LoggingThread.log("Console: Turning off device with IP '" + device.getIpAddress() + "'.");
            device.turnOff();
        } else {
            LoggingThread.log("Console: Turning on device with IP '" + device.getIpAddress() + "'.");
            device.turnOn();
        }
    }

    private @Nullable Device findDeviceById(String id) {
        for (Device device : SharedState.devices) {
            if (device.getId().equalsIgnoreCase(id)) {
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

    private void changeDeviceConfig(String @NotNull [] params, @NotNull Device device) {
        String location = null;
        String name = null;
        String address = null;
        for (int i = 2; i < params.length; i++) {
            try {
                String key = params[i].split("=")[0];
                String value = params[i].split("=")[1];
                switch (key) {
                    case "NAME" -> name = value;
                    case "LOCATION", "LOC" -> location = value;
                    case "ADDRESS", "ADDR" -> address = value;
                    default -> {
                        wrongCommand();
                        return;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                wrongCommand();
                return;
            }
        }
        if (location == null && name == null && address == null) {
            wrongCommand();
            return;
        }
        LoggingThread.log("Console: Changing config for Device with ID '" + device.getId() + "'.");
        if (name != null) {
            device.setName(name);
        }
        if (address != null) {
            device.setIpAddress(address);
        }
        if (location != null) {
            device.setLocation(location);
        }
    }

    private void wrongCommand() {
        System.out.println("Wrong command entered, use 'help' or '?' for a list of commands");
    }
}
