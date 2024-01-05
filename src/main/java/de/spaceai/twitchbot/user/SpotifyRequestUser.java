package de.spaceai.twitchbot.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SpotifyRequestUser {

    private String userId;
    private int requests;
    private long lastRequestTime;

    public void addRequest() {
        this.requests++;
    }

}
