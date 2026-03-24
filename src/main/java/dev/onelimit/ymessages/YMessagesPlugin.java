package dev.onelimit.ymessages;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.onelimit.ymessages.announce.AnnouncementService;
import dev.onelimit.ymessages.command.AnnounceCommand;
import dev.onelimit.ymessages.config.ConfigService;
import dev.onelimit.ymessages.model.PluginConfig;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;

public final class YMessagesPlugin {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private ConfigService configService;
    private AnnouncementService announcementService;
    private PluginConfig config;

    @Inject
    public YMessagesPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        this.configService = new ConfigService(logger, dataDirectory);
        this.announcementService = new AnnouncementService(this, server);

        reload();
        registerCommand();

        logger.info("yMessages initialized.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (announcementService != null) {
            announcementService.shutdown();
        }
    }

    public void reload() {
        this.config = configService.load();
        if (config.debug()) {
            logger.info("Debug mode enabled. Config version: {}", config.configVersion());
        }
        announcementService.updateConfig(config);
        logger.info("Config reloaded successfully.");
    }

    public PluginConfig currentConfig() {
        return config != null ? config : PluginConfig.defaults();
    }

    public Logger logger() {
        return logger;
    }

    private void registerCommand() {
        if (!config.commandEnabled()) {
            logger.info("Announcement command disabled via config.");
            return;
        }

        List<String> aliases = config.commandAliases();
        if (aliases.isEmpty()) {
            aliases = List.of("vannounce");
        }

        String primary = aliases.get(0);
        String[] secondary = aliases.size() > 1
            ? aliases.subList(1, aliases.size()).toArray(String[]::new)
            : new String[0];

        CommandManager manager = server.getCommandManager();
        CommandMeta meta = manager.metaBuilder(primary)
            .aliases(secondary)
            .plugin(this)
            .build();

        manager.register(meta, new AnnounceCommand(this, announcementService));
    }
}

