package de.spaceai.twitchbot.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

import lombok.Getter;


@Getter
public class ConfigOptions {

    private boolean SpotifyModuleActive = false;
    private boolean CommandsOnlyLive = false;
    
    private String TwitchClientId = "";
    private String TwitchSecret = "";
    private String TwitchAccessToken = "";
    private String channel = "";
    private String SpotifyClientId = "";
    private String SpotifyClientSecret = "";
    private String SpotifyRefreshToken = "";

    private boolean followMessageEnabled = false;
    private String followMessage = "Willkommen @%name%, danke das du gefolgt bist.";

    private boolean WebHookEnabled = false;
    private String WebHookUrl = "";
    private WebhookOptions webhookOptions = new WebhookOptions();

    public String getWebhookAsJson() {
        return new Gson().toJson(this.webhookOptions);
    }


    public class WebhookOptions {
        private String username = "Stream";
        private String avatar_url = "https://play-lh.googleusercontent.com/QLQzL-MXtxKEDlbhrQCDw-REiDsA9glUH4m16syfar_KVLRXlzOhN7tmAceiPerv4Jg=w240-h480-rw";
        private String content = "@everyone";
        @Getter
        private List<EmbedOptions> embeds = Arrays.asList(new EmbedOptions());
    }

    public class EmbedOptions {
        private String title = "Stream";
        private String url = "";
        private int color = 0xc0392b;
        private String description = "Ich bin nun Live, schau mir zu";
        @Getter
        private final Map<String, String> image = Maps.newHashMap();
    }

}
