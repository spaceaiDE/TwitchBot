package de.spaceai.twitchbot.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.spaceai.twitchbot.util.Logger;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ConfigProvider {

    private File file;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    private ConfigOptions configOptions;

    @SneakyThrows
    public ConfigProvider(String filePath) {
        this.file = new File(filePath);
        if (!this.file.exists()) {
            this.createNewConfig();
            Logger.from("Config").log("Config Created, please update the config");
            System.exit(0);
            return;
        }
        this.configOptions = this.gson.fromJson(new FileReader(this.file), ConfigOptions.class);
    }

    @SneakyThrows
    private void createNewConfig() {
        this.file.createNewFile();
        this.configOptions = new ConfigOptions();
        String jsonString = this.gson.toJson(this.configOptions);
        FileWriter fileWriter = new FileWriter(this.file);
        fileWriter.write(jsonString);
        fileWriter.flush();
        fileWriter.close();
    }

}
