package de.spaceai.twitchbot;

import de.spaceai.twitchbot.core.TwitchApp;
import lombok.Getter;

public class Bootstrap {

    @Getter
    private static TwitchApp twitchApp;

    public static void main(String[] args) {
        twitchApp = new TwitchApp();
        twitchApp.registerEvents();
    }

}