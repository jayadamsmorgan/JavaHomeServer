package com.purpleclique.javahomeserver;

import com.purpleclique.javahomeserver.threads.*;
import com.purpleclique.javahomeserver.utils.DBUtil;
import com.purpleclique.javahomeserver.utils.NetworkManager;
import com.purpleclique.javahomeserver.utils.Properties;
import com.purpleclique.javahomeserver.utils.SharedState;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sun.misc.Signal;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

@SpringBootApplication
@RequiredArgsConstructor
public class JavaHomeServerApplication {

	private static final String version = "0.69";

	public static void main(String[] args) {

		SpringApplication.run(JavaHomeServerApplication.class, args);

		welcomeLines();

		parseProperties();

		loadDevices();

		threadsInit();

		String ip = null;
		try (DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 53);
			ip = socket.getLocalAddress().getHostAddress();
		} catch (SocketException | UnknownHostException ignored) { }

		String logString = "JavaHomeServer started";

		if (ip != null) {
			logString += " at " + ip;
		}

		logString += ". UDP port: " + NetworkManager.PORT + ", Spring Boot port: 8080";

		LoggingThread.logWarning(logString);

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

	private static int parseLogLevel(@NonNull String logLevel) {
		switch (logLevel) {
			case "0", "NONE" -> {
				logLevel = "0";
				System.out.println(LoggingThread.ANSI_PURPLE + "NONE" + LoggingThread.ANSI_RESET + ".");
			}
			case "2", "WARNING" -> {
				logLevel = "2";
				System.out.println(LoggingThread.ANSI_YELLOW + "WARNING" + LoggingThread.ANSI_RESET + ".");
			}
			case "3", "VERBOSE" -> {
				logLevel = "3";
				System.out.println(LoggingThread.ANSI_GREEN + "VERBOSE" + LoggingThread.ANSI_RESET + ".");
			}
			default -> {
				logLevel = "1";
				System.out.println(LoggingThread.ANSI_RED + "ERROR" + LoggingThread.ANSI_RESET + ".");
			}
		}
		return Integer.parseInt(logLevel);
	}

	private static void parseProperties() {

		System.out.print("Console output logging level: ");
		LoggingThread.consoleLogLevel = parseLogLevel(Properties.getConsoleLogLevel());

		System.out.print("File output logging level: ");
		LoggingThread.fileLogLevel = parseLogLevel(Properties.getFileLogLevel());
	}

	private static void loadDevices() {
		System.out.println("Starting database...");
		var db = DBUtil.getInstance();
		System.out.print("Loading Devices... ");
		SharedState.devices.addAll(db.getAllDevices());
		if (SharedState.devices.isEmpty()) {
			System.out.println("No devices available yet.");
		} else {
			System.out.println("Done. Device count: " + SharedState.devices.size() + ".");
		}
	}

	private static void threadsInit() {
		System.out.print("Initializing threads");

		startNewThread(new LoggingThread());
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
