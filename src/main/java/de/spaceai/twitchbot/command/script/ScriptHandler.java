package de.spaceai.twitchbot.command.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import de.spaceai.twitchbot.command.script.request.RequestHandler;
import de.spaceai.twitchbot.core.TwitchApp;
import de.spaceai.twitchbot.util.Logger;
import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class ScriptHandler {

    private ScriptEngine engine;
    private Bindings bindings;
    private Logger logger;
    private RequestHandler requestHandler;
    private final TwitchApp twitchApp;

    public ScriptHandler(TwitchApp twitchApp) {
        this.requestHandler = new RequestHandler();
        this.engine = new ScriptEngineManager().getEngineByName("nashorn");
        this.logger = Logger.from("Script Engine");
        this.bindings = new SimpleBindings();
        this.twitchApp = twitchApp;

        /**
         * Create bindings
         */
        this.bindings.put("console", this.logger);
        this.bindings.put("http", this.requestHandler);
        this.bindings.put("file", new ScriptFileHandle());
        this.bindings.put("Config", this.twitchApp.getConfigProvider().getConfigOptions());

        this.engine.setBindings(this.bindings, ScriptContext.GLOBAL_SCOPE);
    }

    public class ScriptFileHandle {
        @SneakyThrows
        public void create(String name) {
            File file = new File(name);
            if(!exists(name)) {
                Files.createDirectories(Path.of(name));
                file.createNewFile();
            }
        }
        public boolean exists(String name) {
            return new File(name).exists();
        }
        @SneakyThrows
        public void write(String name, String content) {
            FileWriter writer = new FileWriter(name);
            writer.write(content);
            writer.flush();
            writer.close();
        }
        @SneakyThrows
        public String read(String name) {
            FileReader fileReader = new FileReader(name);
            String line = "";
            String content = "";
            BufferedReader reader = new BufferedReader(fileReader);
            while((line = reader.readLine()) != null) {
                content += line + "\n";
            }
            reader.close();
            return content;
        }
    }

}
