package de.spaceai.twitchbot.command.impl;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import de.spaceai.twitchbot.Bootstrap;
import de.spaceai.twitchbot.command.Command;
import de.spaceai.twitchbot.core.SpotifyApp;
import de.spaceai.twitchbot.util.Logger;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Arrays;
import java.util.List;

public class SkipCommand extends Command {

    public SkipCommand() {
        super("skip", true);
    }

    @Override
    public void execute(TwitchChat chat, EventChannel channel, EventUser user, String[] args) {
        SpotifyApp spotifyApp = Bootstrap.getTwitchApp().getSpotifyApp();
        try {
            spotifyApp.getSpotifyApi().skipUsersPlaybackToNextTrack().build().execute();
            Thread.sleep(1000);
            CurrentlyPlayingContext context = spotifyApp.getSpotifyApi().getInformationAboutUsersCurrentPlayback().build()
                    .execute();
            Track track = (Track) context.getItem();
            List<String> artists = Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).toList();
            chat.sendActionMessage(channel.getName(),
                    "Spiele nun '" + track.getName() + " - " + String.join(", ", artists) + "'");
        } catch (Exception e) {
            chat.sendActionMessage(channel.getName(), "Derzeit spielt Spotify keine Musik ab.");
            Logger.from(getCommandName()).log(e.getMessage());
        }
    }
}
