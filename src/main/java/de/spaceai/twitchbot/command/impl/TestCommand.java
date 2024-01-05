package de.spaceai.twitchbot.command.impl;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import de.spaceai.twitchbot.command.Command;

public class TestCommand extends Command {
    public TestCommand() {
        super("test", true);
    }

    @Override
    public void execute(TwitchChat chat, EventChannel channel, EventUser user, String[] args) {
        chat.sendMessage(channel.getName(), "Hallo, @" + user.getName());
    }
}
