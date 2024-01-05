package de.spaceai.twitchbot.core;

import de.spaceai.twitchbot.util.Logger;
import lombok.Getter;
import lombok.SneakyThrows;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

@Getter
public class SpotifyApp {

    private final SpotifyApi spotifyApi;
    private final TwitchApp twitchApp;
    private long lastRefresh = System.currentTimeMillis() + (1000*60*59);

    public SpotifyApp(TwitchApp twitchApp) {
        this.twitchApp = twitchApp;
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(this.twitchApp.getConfigProvider().getConfigOptions().getSpotifyClientId())
                .setClientSecret(this.twitchApp.getConfigProvider().getConfigOptions().getSpotifyClientSecret())
                .setRefreshToken(this.twitchApp.getConfigProvider().getConfigOptions().getSpotifyRefreshToken())
                .build();

        try {
            AuthorizationCodeCredentials code = this.spotifyApi.authorizationCodeRefresh().build().execute();
            this.spotifyApi.setAccessToken(code.getAccessToken());
            Logger.from("Spotify").log("Connected with Spotify");
            this.refreshTokenAfterTime();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void refreshTokenAfterTime() {
        new Thread() {
            @SneakyThrows
            public void run() {
                while(true) {
                    long difference = (lastRefresh - System.currentTimeMillis());
                    if(difference <= 1000*60) {
                        AuthorizationCodeCredentials code = spotifyApi.authorizationCodeRefresh().build().execute();
                        spotifyApi.setAccessToken(code.getAccessToken());
                        Logger.from("Spotify").log("Refreshed Spotify Access Token");
                        lastRefresh = System.currentTimeMillis() + (1000*60*59);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            };
        }.start();
    }

}
