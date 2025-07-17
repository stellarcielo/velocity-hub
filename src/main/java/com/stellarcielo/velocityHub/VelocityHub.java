package com.stellarcielo.velocityHub;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.command.SimpleCommand;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bstats.velocity.Metrics;

import org.slf4j.Logger;

import java.io.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Plugin(
        id = "velocity-hub",
        name = "velocity-hub",
        version = "1.8-SNAPSHOT",
        authors = {"stellarcielo"}
)

public class VelocityHub {

    private final ProxyServer server;
    private final Logger logger;
    private JsonObject config;
    @Inject
    private final Metrics.Factory metricsFactory;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Velocity-hub plugin initialized");

        final int pluginId = 24768;
        final Metrics metrics = metricsFactory.make(this, pluginId);

        versionChecker checker = new versionChecker("velocity-hub-command", "1.8-SNAPSHOT",logger);
        checker.checkForNewRelease();
    }

    @Inject
    public VelocityHub(ProxyServer server, Logger logger, Metrics.Factory metricsFactory) {

        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;

        logger.info("bStats has been initialized.");

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
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonObject defaultConfig = new JsonObject();
                defaultConfig.addProperty("hubServerName", "hub");
                defaultConfig.addProperty("transferMessage", "Sending you to the hub!");
                defaultConfig.addProperty("alreadyConnectedMessage", "You are already connected to the hub!");
                defaultConfig.addProperty("serverNotAvailableMessage", "The hub server is not available.");

                writer.write(gson.toJson(defaultConfig));
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

        private static final MiniMessage miniMessage = MiniMessage.miniMessage();

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
                    sendMiniMessage(player, alreadyConnectedMessage);
                }  else {
                    server.getServer(hubServerName).ifPresentOrElse(serverInfo -> {
                        player.createConnectionRequest(serverInfo).fireAndForget();
                        sendMiniMessage(player, transferMessage);
                    }, () -> {
                        sendMiniMessage(player, serverNotAvailableMessage);
                    });
                }
            }, () -> {
                sendMiniMessage(player, "<red>Unable to determine your current server.");
            });
        }

        public static void sendMiniMessage(Player player, String miniMassageText) {
            Component message = miniMessage.deserialize(miniMassageText);
            player.sendMessage(player, message);
        }
    }
}