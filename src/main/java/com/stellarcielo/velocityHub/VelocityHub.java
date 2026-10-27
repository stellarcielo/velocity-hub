package com.stellarcielo.velocityHub;

import com.google.inject.Inject;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.command.SimpleCommand;

import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bstats.velocity.Metrics;

import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;

@Plugin(
        id = "velocity-hub",
        name = "velocity-hub",
        version = "1.11-SNAPSHOT",
        authors = {"stellarcielo"}
)

public class VelocityHub {

    private final Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;
    private final Metrics.Factory metricsFactory;

    @Inject
    public VelocityHub(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Velocity-hub plugin initialized");

        final int pluginId = 24768;
        final Metrics metrics = metricsFactory.make(this, pluginId);

        PluginConfig config = PluginConfig.load(dataDirectory, logger);
        registerCommands(config);

        String currentVersion = server.getPluginManager().fromInstance(this)
                .flatMap(container -> container.getDescription().getVersion())
                .orElse("unknown");
        VersionChecker checker = new VersionChecker("velocity-hub-command", currentVersion, logger);
        checker.checkForNewRelease();
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        logger.info("Player {} has logged in!", event.getPlayer().getUsername());
    }

    private void registerCommands(PluginConfig config) {
        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("hub").aliases("lobby").build(),
                new HubCommand(server, config, logger)
        );
        logger.info("Commands /hub and /lobby have been registered!");
    }

    public static class HubCommand implements SimpleCommand {
        private final ProxyServer server;
        private final PluginConfig config;
        private final Logger logger;

        private static final MiniMessage miniMessage = MiniMessage.miniMessage();

        public HubCommand(ProxyServer server, PluginConfig config, Logger logger) {
            this.server = server;
            this.config = config;
            this.logger = logger;
        }

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();

            if (!(source instanceof Player player)) {
                sendMiniMessage(source, config.playerOnlyMessage());
                return;
            }

            String hubServerName = config.hubServerName();

            Optional<RegisteredServer> hubServer = server.getServer(hubServerName);
            if (hubServer.isEmpty()) {
                logger.warn("Hub server \"{}\" is not registered in velocity.toml. Check hubServerName in config.json.", hubServerName);
                sendMiniMessage(player, config.serverNotAvailableMessage());
                return;
            }

            boolean alreadyOnHub = player.getCurrentServer()
                    .map(currentServer -> currentServer.getServerInfo().getName().equalsIgnoreCase(hubServerName))
                    .orElse(false);
            if (alreadyOnHub) {
                sendMiniMessage(player, config.alreadyConnectedMessage());
                return;
            }

            sendMiniMessage(player, config.transferMessage());

            player.createConnectionRequest(hubServer.get()).connect().whenComplete((result, throwable) -> {
                if (throwable != null) {
                    logger.warn("Failed to send {} to the hub server \"{}\"", player.getUsername(), hubServerName, throwable);
                    sendMiniMessage(player, config.serverNotAvailableMessage());
                    return;
                }

                ConnectionRequestBuilder.Status status = result.getStatus();
                if (status == ConnectionRequestBuilder.Status.ALREADY_CONNECTED) {
                    sendMiniMessage(player, config.alreadyConnectedMessage());
                } else if (status == ConnectionRequestBuilder.Status.CONNECTION_CANCELLED
                        || status == ConnectionRequestBuilder.Status.SERVER_DISCONNECTED) {
                    sendMiniMessage(player, config.serverNotAvailableMessage());
                }
                // SUCCESS and CONNECTION_IN_PROGRESS need no extra message
            });
        }

        public static void sendMiniMessage(CommandSource source, String miniMessageText) {
            source.sendMessage(miniMessage.deserialize(miniMessageText));
        }
    }
}
