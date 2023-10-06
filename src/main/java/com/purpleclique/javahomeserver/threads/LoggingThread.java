package com.purpleclique.javahomeserver.threads;

import lombok.NonNull;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.LinkedBlockingQueue;

public class LoggingThread implements Runnable {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_PURPLE = "\u001B[35m";

    public static final int LOG_LEVEL_NONE = 0;
    public static final int LOG_LEVEL_ERROR = 1;
    public static final int LOG_LEVEL_WARNING = 2;
    public static final int LOG_LEVEL_VERBOSE = 3;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

    private static LinkedBlockingQueue<String> loggingQueue;

    public static int fileLogLevel;
    public static int consoleLogLevel;

    public LoggingThread() {
        loggingQueue = new LinkedBlockingQueue<>();
        try {
            File logFile = new File("log.txt");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void clearLogFile() {
        try (PrintStream logPrintStream = new PrintStream(
                new FileOutputStream("log.txt"))) {
            logPrintStream.println();
            logPrintStream.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        log("Console: 'log.txt' cleared.");
    }

    public static void logError(String message) {
        loggingQueue.add("E" + message);
    }

    public static void logWarning(String message) {
        loggingQueue.add("W" + message);
    }

    public static void log(String message) {
        loggingQueue.add("M" + message);
    }

    private static @NonNull String getCurrentDateAndTime() {
        return dtf.format(LocalDateTime.now());
    }

    public void run() {
        while (true) {
            try {
                String logMessage = loggingQueue.take();
                String textMessage = "";
                String consoleMessage = "";
                String message = logMessage.substring(1);
                int logLevel = LOG_LEVEL_NONE;
                if (logMessage.startsWith("E")) {
                    textMessage = "[" + getCurrentDateAndTime() + "]: " + ANSI_RED + "ERROR: " + message + ANSI_RESET;
                    consoleMessage = "[" + getCurrentDateAndTime() + "]: " + "ERROR: " + message;
                    logLevel = LOG_LEVEL_ERROR;
                }
                if (logMessage.startsWith("W")) {
                    textMessage = "[" + getCurrentDateAndTime() + "]: " + ANSI_YELLOW + "WARNING: " + message + ANSI_RESET;
                    consoleMessage = "[" + getCurrentDateAndTime() + "]: " + "WARNING: " + message;
                    logLevel = LOG_LEVEL_WARNING;
                }
                if (logMessage.startsWith("M")) {
                    consoleMessage = "[" + getCurrentDateAndTime() + "]: " + message;
                    textMessage = consoleMessage;
                    logLevel = LOG_LEVEL_VERBOSE;
                }
                if (!textMessage.equals("")) {
                    if (logLevel <= consoleLogLevel) {
                        System.out.println(textMessage);
                    }
                    if (logLevel <= fileLogLevel) {
                        try (PrintStream logPrintStream = new PrintStream(
                                new FileOutputStream("log.txt", true))) {
                            logPrintStream.println(consoleMessage);
                            logPrintStream.flush();
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
