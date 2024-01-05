package de.spaceai.twitchbot.command.impl;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import de.spaceai.twitchbot.Bootstrap;
import de.spaceai.twitchbot.command.Command;
import de.spaceai.twitchbot.core.SpotifyApp;
import de.spaceai.twitchbot.user.SpotifyRequestUser;
import de.spaceai.twitchbot.util.Logger;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RequestCommand extends Command {

    private List<SpotifyRequestUser> requestUsers = new ArrayList<>();

    public RequestCommand() {
        super("request", false);
    }

    @Override
    public void execute(TwitchChat chat, EventChannel channel, EventUser user, String[] args) {

        SpotifyRequestUser requestUser = this.requestUsers.stream().filter(u -> u.getUserId().equals(user.getId()))
                .findFirst().orElse(null);

        if (requestUser == null) {
            requestUser = new SpotifyRequestUser(user.getId(), 0, System.currentTimeMillis());
            this.requestUsers.add(requestUser);
        }

        long requestDiff = (System.currentTimeMillis() - requestUser.getLastRequestTime());

        if (requestUser.getRequests() > 5 && requestDiff <= 1000 * 60 * 30) {
            chat.sendMessage(channel.getName(), "@" + user.getName() + ", du hast dein Limit schon aufgebraucht.");
            return;
        }

        String name = "";
        for (String arg : args) {
            name += arg + " ";
        }
        name = name.trim();

        SpotifyApp spotifyApp = Bootstrap.getTwitchApp().getSpotifyApp();
        if (name.startsWith("https://open.spotify.com")) {
            String id = name.split("track/")[1].split("\\?")[0];
            try {
                Track track = spotifyApp.getSpotifyApi().getTrack(id).build().execute();
                spotifyApp.getSpotifyApi().addItemToUsersPlaybackQueue(track.getUri())
                        .build().execute();
                List<String> artists = Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).toList();
                chat.sendActionMessage(channel.getName(), "'" + track.getName() + " - " + String.join(", ", artists)
                        + "' wurde in die Warteschlange hinzugefügt.");
                if (requestDiff <= 1000 * 60 * 30) {
                    requestUser.addRequest();
                } else {
                    requestUser.setLastRequestTime(System.currentTimeMillis());
                    requestUser.setRequests(1);
                }
            } catch (Exception e) {
                chat.sendActionMessage(channel.getName(), "Derzeit spielt Spotify keine Musik ab.");
                Logger.from("Spotify Command").log(e.getMessage());
            }
            return;
        }
        try {
            Paging<Track> tracks = spotifyApp.getSpotifyApi().searchTracks(name).build().execute();
            if (tracks.getItems().length == 0)
                return;
            Track track = tracks.getItems()[0];
            spotifyApp.getSpotifyApi().addItemToUsersPlaybackQueue(track.getUri())
                    .build().execute();
            List<String> artists = Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).toList();
            chat.sendActionMessage(channel.getName(), "'" + track.getName() + " - " + String.join(", ", artists)
                    + "' wurde in die Warteschlange hinzugefügt.");
            if (requestDiff <= 1000 * 60 * 30) {
                requestUser.addRequest();
            } else {
                requestUser.setLastRequestTime(System.currentTimeMillis());
                requestUser.setRequests(1);
            }
        } catch (Exception e) {
            chat.sendActionMessage(channel.getName(), "Derzeit spielt Spotify keine Musik ab.");
            Logger.from("Spotify Command").log(e.getMessage());
        }
    }
}
