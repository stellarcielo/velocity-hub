package com.stellarcielo.velocityHub;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PluginConfig {

    private static final String FILE_NAME = "config.json";

    // MiniMessage tags such as <red> must not be escaped to < when the file is rewritten
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final Map<String, String> DEFAULTS = new LinkedHashMap<>();

    static {
        DEFAULTS.put("hubServerName", "hub");
        DEFAULTS.put("transferMessage", "Sending you to the hub!");
        DEFAULTS.put("alreadyConnectedMessage", "You are already connected to the hub!");
        DEFAULTS.put("serverNotAvailableMessage", "The hub server is not available.");
        DEFAULTS.put("playerOnlyMessage", "Only players can execute this command!");
    }

    private final Map<String, String> values;

    private PluginConfig(Map<String, String> values) {
        this.values = values;
    }

    public String hubServerName() {
        return values.get("hubServerName");
    }

    public String transferMessage() {
        return values.get("transferMessage");
    }

    public String alreadyConnectedMessage() {
        return values.get("alreadyConnectedMessage");
    }

    public String serverNotAvailableMessage() {
        return values.get("serverNotAvailableMessage");
    }

    public String playerOnlyMessage() {
        return values.get("playerOnlyMessage");
    }

    public static PluginConfig load(Path dataDirectory, Logger logger) {
        Path configFile = dataDirectory.resolve(FILE_NAME);

        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            logger.error("Failed to create plugin directory {}", dataDirectory.toAbsolutePath(), e);
        }

        if (!Files.exists(configFile)) {
            JsonObject defaultConfig = new JsonObject();
            DEFAULTS.forEach(defaultConfig::addProperty);
            if (save(configFile, defaultConfig, logger)) {
                logger.info("Default config created at {}", configFile.toAbsolutePath());
            }
            return new PluginConfig(new LinkedHashMap<>(DEFAULTS));
        }

        JsonObject json;
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                throw new JsonParseException("The root of the config must be a JSON object");
            }
            json = root.getAsJsonObject();
        } catch (IOException | JsonParseException e) {
            // Keep the broken file as-is so the user can fix it instead of losing their settings
            logger.error("Failed to load {}. Default values are used until the file is fixed.", configFile.toAbsolutePath(), e);
            return new PluginConfig(new LinkedHashMap<>(DEFAULTS));
        }

        Map<String, String> values = new LinkedHashMap<>();
        List<String> addedKeys = new ArrayList<>();

        for (Map.Entry<String, String> entry : DEFAULTS.entrySet()) {
            String key = entry.getKey();
            JsonElement element = json.get(key);

            if (element == null || element.isJsonNull()) {
                json.addProperty(key, entry.getValue());
                values.put(key, entry.getValue());
                addedKeys.add(key);
            } else if (element.isJsonPrimitive()) {
                values.put(key, element.getAsString());
            } else {
                logger.warn("Config value \"{}\" must be a string. The default value is used instead.", key);
                values.put(key, entry.getValue());
            }
        }

        if (!addedKeys.isEmpty() && save(configFile, json, logger)) {
            logger.info("Added missing config keys {} to {}", addedKeys, configFile.toAbsolutePath());
        }

        logger.info("Config loaded successfully from {}", configFile.toAbsolutePath());
        return new PluginConfig(values);
    }

    private static boolean save(Path configFile, JsonObject json, Logger logger) {
        try (Writer writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
            writer.write(GSON.toJson(json));
            return true;
        } catch (IOException e) {
            logger.error("Failed to write config file {}", configFile.toAbsolutePath(), e);
            return false;
        }
    }
}
