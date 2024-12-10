package com.stellarcielo.velocityHub;

import com.google.inject.Inject;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.command.SimpleCommand;

import net.kyori.adventure.text.Component;

import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Plugin(
        id = "velocity-hub",
        name = "velocity-hub",
        version = "1.3-SNAPSHOT",
        authors = {"stellarcielo"}
)

public class VelocityHub {

    private final ProxyServer server;
    private final Logger logger;
    private JsonObject config;

    @Inject
    public VelocityHub(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        loadConfig();
        registerCommands();
    }

    private void loadConfig() {
        File configFolder = new File("plugins/velocity-hub");
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        File configFile = new File(configFolder, "config.json");

        if (!configFile.exists()) {
            try (FileWriter writer = new FileWriter(configFile)){
                JsonObject defaultConfig = new JsonObject();
                defaultConfig.addProperty("hubServerName", "hub");
                defaultConfig.addProperty("transferMessage", "Sending you to the hub!");
                defaultConfig.addProperty("alreadyConnectedMessage", "You are already connected to the hub!");
                defaultConfig.addProperty("serverNotAvailableMessage", "The hub server is not available.");

                writer.write(defaultConfig.toString());
                this.config = defaultConfig;
                logger.info("Default config created at " + configFile.getAbsolutePath());
            } catch (IOException e) {
                logger.error("Failed to create default config file!", e);
            }
        } else {
            try (FileReader reader = new FileReader(configFile)) {
                this.config = JsonParser.parseReader(reader).getAsJsonObject();
                logger.info("Config loaded successfully from " + configFile.getAbsolutePath());
            } catch (IOException e) {
                logger.error("Failed to load config file!", e);
            }
        }
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        logger.info("Player {} has logged in!", event.getPlayer().getUsername());
    }

    public void registerCommands(){
        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("hub").build(),
                new HubCommand(server, config, logger)
        );
        logger.info("Command /hub has been registered!");
    }

    public static class HubCommand implements SimpleCommand {
        private final ProxyServer server;
        private final JsonObject config;
        private final Logger logger;

        public HubCommand(ProxyServer server, JsonObject config, Logger logger) {
            this.server = server;
            this.config = config;
            this.logger = logger;
        }

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();

            if(!(source instanceof Player)){
                source.sendMessage(Component.text("Only players can execute this command!"));
                return;
            }

            Player player = (Player) source;

            String hubServerName = config.get("hubServerName").getAsString();
            String alreadyConnectedMessage = config.get("alreadyConnectedMessage").getAsString();
            String transferMessage = config.get("transferMessage").getAsString();
            String serverNotAvailableMessage = config.get("serverNotAvailableMessage").getAsString();

            player.getCurrentServer().ifPresentOrElse(currentServer -> {
                if (currentServer.getServerInfo().getName().equalsIgnoreCase(hubServerName)) {
                    player.sendMessage(Component.text(alreadyConnectedMessage));
                }  else {
                    server.getServer(hubServerName).ifPresentOrElse(serverInfo -> {
                        player.createConnectionRequest(serverInfo).fireAndForget();
                        player.sendMessage(Component.text(transferMessage));
                    }, () -> {
                        player.sendMessage(Component.text(serverNotAvailableMessage));
                    });
                }
            }, () -> {
                player.sendMessage(Component.text("Unable to determine your current server."));
            });
        }
    }

    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}