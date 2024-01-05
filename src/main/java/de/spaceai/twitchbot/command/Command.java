package de.spaceai.twitchbot.command;


import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public abstract class Command {

    private final String commandName;

    @Setter
    private boolean onlyTeam = false;

    public abstract void execute(TwitchChat chat, EventChannel channel, EventUser user, String[] args);

}
