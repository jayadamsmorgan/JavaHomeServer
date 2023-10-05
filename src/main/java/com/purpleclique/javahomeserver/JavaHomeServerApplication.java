package com.purpleclique.javahomeserver;

import com.purpleclique.javahomeserver.threads.*;
import com.purpleclique.javahomeserver.utils.DBUtil;
import com.purpleclique.javahomeserver.utils.SharedState;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sun.misc.Signal;

@SpringBootApplication
public class JavaHomeServerApplication {

	private static final String version = "0.69";

	private static boolean isConsoleLoggingEnabled = true;
	private static boolean isFileLoggingEnabled = true;

	@Value("${homeserver.log_level.console}")
	private static int consoleLogLevel;

	@Value("${homeserver.log_level.file}")
	private static int fileLogLevel;

	public static void main(String[] args) {

		SpringApplication.run(JavaHomeServerApplication.class, args);

		welcomeLines();

		parseArguments();

		loadDevices();

		threadsInit();

		LoggingThread.logWarning("Home server started at ");
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

	private static void parseArguments() {

		System.out.print("Console output logging level: ");
		switch (consoleLogLevel) {
			case 0 -> {
				isConsoleLoggingEnabled = false;
				System.out.println(LoggingThread.ANSI_PURPLE + "NONE" + LoggingThread.ANSI_RESET + ".");
			}
			case 2 -> System.out.println(LoggingThread.ANSI_YELLOW + "WARNING" + LoggingThread.ANSI_RESET + ".");
			case 3 -> System.out.println(LoggingThread.ANSI_GREEN + "VERBOSE" + LoggingThread.ANSI_RESET + ".");
			default -> {
				consoleLogLevel = 1;
				System.out.println(LoggingThread.ANSI_RED + "ERROR" + LoggingThread.ANSI_RESET + ".");
			}
		}

		System.out.print("File output logging level: ");
		switch (fileLogLevel) {
			case 0 -> {
				isFileLoggingEnabled = false;
				System.out.println(LoggingThread.ANSI_PURPLE + "NONE" + LoggingThread.ANSI_RESET + ".");
			}
			case 2 -> System.out.println(LoggingThread.ANSI_YELLOW + "WARNING" + LoggingThread.ANSI_RESET + ".");
			case 3 -> System.out.println(LoggingThread.ANSI_GREEN + "VERBOSE" + LoggingThread.ANSI_RESET + ".");
			default -> {
				fileLogLevel = LoggingThread.LOG_LEVEL_ERROR;
				System.out.println(LoggingThread.ANSI_RED + "ERROR" + LoggingThread.ANSI_RESET + ".");
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

		startNewThread(new LoggingThread(isConsoleLoggingEnabled, isFileLoggingEnabled, consoleLogLevel, fileLogLevel));
		startNewThread(new UserInputThread());
		Thread packetReceiveThread = startNewThread(new PacketReceiveThread());
		System.out.print(".");

		startNewThread(new DeviceInputThread());
		startNewThread(new DeviceOutputThread());
		System.out.print(".");

		System.out.print(".");

		System.out.println(" Done.");

		// Shutdown Signal handling
		Signal.handle(new Signal("INT"), sig -> {
			LoggingThread.logWarning("Shutting down Smart Home Server...");
			packetReceiveThread.interrupt();
			LoggingThread.log("Packet receive thread stopped. Waiting for queues to clear...");
			// It's safe to exit if there is nothing in Signal queues.
			while (!SharedState.deviceOutputSignals.isEmpty()
					|| !SharedState.deviceInputSignals.isEmpty()) {
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

	private static @NonNull Thread startNewThread(Runnable runnable) {
		Thread newThread = new Thread(runnable);
		newThread.start();
		return newThread;
	}


}
