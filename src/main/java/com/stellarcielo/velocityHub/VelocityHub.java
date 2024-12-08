package com.stellarcielo.velocityHub;

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

import org.slf4j.Logger;

import java.util.Optional;

@Plugin(
        id = "velocity-hub",
        name = "velocity-hub",
        version = "0.1-SNAPSHOT",
        authors = {"stellarcielo"}
)
public class VelocityHub {

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public VelocityHub(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        registerCommands();
        logger.info("Command /hub has been registered!");
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        logger.info("Player {} has logged in!", event.getPlayer().getUsername());
    }

    public void registerCommands(){
        server.getCommandManager().register("hub", new HubCommand(server));
    }

    public static class HubCommand implements SimpleCommand {
        private final ProxyServer server;

        public HubCommand(ProxyServer server) {
            this.server = server;
        }

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();

            if(!(source instanceof Player)){
                source.sendMessage(Component.text("Only players can execute this command!"));
                return;
            }

            Player player = (Player) source;

            Optional<String> hubServerName = Optional.of("hub");

            hubServerName.ifPresent(serverName -> {
                server.getServer(serverName).ifPresentOrElse(serverInfo -> {
                    player.createConnectionRequest(serverInfo).fireAndForget();
                    player.sendMessage(Component.text("Sending you to the hub!"));
                }, () -> {
                    player.sendMessage(Component.text("The hub server is not available."));
                });
            });
        }
    }

    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
