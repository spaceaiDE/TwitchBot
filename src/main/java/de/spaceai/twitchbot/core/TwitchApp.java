package de.spaceai.twitchbot.core;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.FollowEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.google.gson.Gson;

import de.spaceai.twitchbot.command.Command;
import de.spaceai.twitchbot.command.CommandHandler;
import de.spaceai.twitchbot.command.script.ScriptHandler;
import de.spaceai.twitchbot.command.script.request.RequestHandler;
import de.spaceai.twitchbot.config.ConfigOptions;
import de.spaceai.twitchbot.config.ConfigProvider;
import de.spaceai.twitchbot.util.Logger;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
public class TwitchApp {

    private TwitchClient twitchClient;
    private TwitchChat twitchChat;
    private CommandHandler commandHandler;
    private SpotifyApp spotifyApp;
    private ConfigProvider configProvider;
    private ScriptHandler scriptHandler;

    private String baseUrl = "https://static-cdn.jtvnw.net/previews-ttv/live_user_%channel%-1920x1080.jpg";

    @Setter
    private boolean live = false;

    public TwitchApp() {
        this.configProvider = new ConfigProvider("config.json");
        this.scriptHandler = new ScriptHandler(this);

        if (this.configProvider.getConfigOptions().isSpotifyModuleActive()) {
            this.spotifyApp = new SpotifyApp(this);
        }

        this.commandHandler = new CommandHandler(this);
        this.commandHandler.registerCommands();

        OAuth2Credential credential = new OAuth2Credential("twitch", this.configProvider.getConfigOptions().getTwitchAccessToken());

        CredentialManager credentialManager = CredentialManagerBuilder.builder().build();

        TwitchIdentityProvider identityProvider = new TwitchIdentityProvider(
            this.configProvider.getConfigOptions().getTwitchClientId(), 
            this.configProvider.getConfigOptions().getTwitchSecret(), 
            "https://twitchtokengenerator.com");
        credentialManager.registerIdentityProvider(identityProvider);

        credential.updateCredential(credential);

        this.twitchClient = TwitchClientBuilder.builder()
            .withEnableHelix(true)
            .withCredentialManager(credentialManager)
            .withDefaultAuthToken(credential)
            .withEnablePubSub(true)
            .withChatAccount(
                credential)
            .withEnableChat(true)
            .build();

        this.twitchChat = this.twitchClient.getChat();
        String channel = this.configProvider.getConfigOptions().getChannel();
        if(!this.twitchChat.isChannelJoined(channel)) 
            this.twitchChat.joinChannel(channel);

        this.baseUrl = this.getBaseUrl().replaceAll("%channel%", channel);
            
        this.twitchClient.getClientHelper().enableStreamEventListener(channel);
        this.twitchClient.getClientHelper().enableFollowEventListener(channel);

        this.configProvider.getConfigOptions().getWebhookOptions().getEmbeds().forEach(embed -> {
            if(!embed.getImage().containsKey("url")) {
                embed.getImage().put("url", this.baseUrl);
            }
        });


        Logger.from("Twitch").log("Twitch Client connected");
    }

    public void registerEvents() {

        this.twitchClient.getEventManager().onEvent(FollowEvent.class, event -> {
            if(!this.configProvider.getConfigOptions().isFollowMessageEnabled()) return;
            this.twitchChat.sendActionMessage(this.configProvider.getConfigOptions().getChannel(), 
                this.configProvider.getConfigOptions().getFollowMessage().replaceAll("%name%", event.getUser().getName()));
        });

        this.twitchClient.getEventManager().onEvent(ChannelGoLiveEvent.class, (event) -> {
            Logger.from("Twitch").log("Changed Live Status to: true");
            setLive(true);
            /**
             * Discord Webhook Send
             */

            ConfigOptions configOptions = this.configProvider.getConfigOptions();

            if(!configOptions.isWebHookEnabled()) return;
            RequestHandler requestHandler = new RequestHandler();
            requestHandler.post(configOptions.getWebHookUrl(), new Gson().toJson(configOptions.getWebhookOptions()), "application/json", null);
        });

        this.twitchClient.getEventManager().onEvent(ChannelGoOfflineEvent.class, (event) -> {
            Logger.from("Twitch").log("Changed Live Status to: false");
            setLive(false);
        });

        this.twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
            Logger.from("Twitch Chat").log(
                    "[" + event.getChannel().getName() + "] " + event.getUser().getName() + ": " + event.getMessage());
            String message = event.getMessage();
            EventChannel channel = event.getChannel();
            EventUser user = event.getUser();

            if(!message.startsWith("!")) return;

            if(!isLive() && this.configProvider.getConfigOptions().isCommandsOnlyLive()) {
                Logger.from("Twitch Chat").log("Could not compute commands, channel is not live");
                return;
            }

            String[] arguments = message.split(" ");
            String command = arguments[0];
            String finalCommand = command;
            arguments = Arrays.stream(arguments).filter(str -> !finalCommand.equals(str)).toArray(String[]::new);
            command = command.replaceAll("!", "");

            String finalCommand1 = command;
            Command cmd = this.commandHandler.getCommands().stream()
                    .filter(c -> c.getCommandName().equalsIgnoreCase(finalCommand1)).findFirst()
                    .orElse(null);

            if (cmd == null)
                return;

            if (cmd.isOnlyTeam()) {
                if (event.getPermissions().contains(CommandPermission.BROADCASTER) ||
                        event.getPermissions().contains(CommandPermission.MODERATOR)) {
                    cmd.execute(event.getTwitchChat(), channel, user, arguments);
                }
            } else {
                cmd.execute(event.getTwitchChat(), channel, user, arguments);
            }

        });
    }

}
