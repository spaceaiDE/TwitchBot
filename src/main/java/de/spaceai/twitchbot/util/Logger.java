package de.spaceai.twitchbot.util;

import java.util.Date;

public class Logger {

    public static Logger from(String name) {
        return new Logger(name);
    }

    private final String name;

    private Logger(String name) {
        this.name = name;
    }

    public void log(String message) {
        System.out.println("[" + new Date().toInstant().toString() + "] " + this.name + " | " + message);
    }

}
