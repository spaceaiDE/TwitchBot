package de.spaceai.twitchbot.command;

import de.spaceai.twitchbot.command.impl.RequestCommand;
import de.spaceai.twitchbot.command.impl.SkipCommand;
import de.spaceai.twitchbot.command.impl.SongInfoCommand;
import de.spaceai.twitchbot.command.impl.TestCommand;
import de.spaceai.twitchbot.command.script.ScriptHandler;
import de.spaceai.twitchbot.core.TwitchApp;
import de.spaceai.twitchbot.util.Logger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.script.Invocable;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;

@Getter
@RequiredArgsConstructor
public class CommandHandler {

    private List<Command> commands = new ArrayList<>();
    private final TwitchApp twitchApp;

    public void registerCommands() {
        /**
         * Registers all commands
         */
        addCommand(new TestCommand());
        if (this.twitchApp.getConfigProvider().getConfigOptions().isSpotifyModuleActive()) {
            addCommand(new RequestCommand());
            addCommand(new SkipCommand());
            addCommand(new SongInfoCommand());
        }

        File directory = new File("commands");
        if (!directory.exists()) {
            directory.mkdir();
        }

        /**
         * Loading Files from Directory
         */
        Arrays.stream(directory.listFiles()).filter(file -> file.getName().endsWith(".js")).forEach(file -> {
            String name = file.getName().substring(0, file.getName().length() - 3);
            
            CompletableFuture.runAsync(() -> {
                try {
                    FileReader reader = new FileReader(file, Charset.forName("UTF-8"));
                    ScriptHandler scriptHandler = new ScriptHandler(twitchApp);
                    scriptHandler.getEngine().eval(reader);
                    reader.close();
                    Invocable invocable = (Invocable) scriptHandler.getEngine();
                    boolean onlyTeam = false;
                    try {
                        Object object = invocable.invokeFunction("isOnlyTeam");
                        if(!(object instanceof Boolean)) {
                            Logger.from("Commands").log("Return type must be a boolean");
                            return;
                        }
                        onlyTeam = (boolean) object;
                    } catch(Exception e) {}
                    Logger.from("Commands").log("Addtional Command " + name + " registered");
                    addCommand(new Command(name, onlyTeam) {
                        @Override
                        public void execute(TwitchChat chat, EventChannel channel, EventUser user, String[] args) {
                            try {
                                invocable.invokeFunction("onChat", chat, channel, user, args);
                            } catch (Exception e) {
                                Logger.from("Commands").log(e.getMessage());
                            }
                        }
                        
                    });
                } catch (Exception e) {
                    Logger.from("Commands").log(e.getMessage());
                }
            });

        });

        Logger.from("Commands").log("Registered " + this.commands.size() + " Commands");

    }

    public void addCommand(Command command) {
        this.commands.add(command);
    }

}
