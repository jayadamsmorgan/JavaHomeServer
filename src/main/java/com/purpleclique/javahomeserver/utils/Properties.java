package com.purpleclique.javahomeserver.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Properties {

    @Getter
    private static String consoleLogLevel;

    @Getter
    private static String fileLogLevel;

    @Value("${homeserver.console-log-level}")
    public void setConsoleLogLevel(String consoleLogLevel) {
        Properties.consoleLogLevel = consoleLogLevel;
    }

    @Value("${homeserver.file-log-level}")
    public void setFileLogLevel(String fileLogLevel) {
        Properties.fileLogLevel = fileLogLevel;
    }
}

