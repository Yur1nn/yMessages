package dev.onelimit.velocityannouces.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import dev.onelimit.velocityannouces.VelocityAnnoucesPlugin;
import dev.onelimit.velocityannouces.announce.AnnouncementService;
import dev.onelimit.velocityannouces.model.AnnounceMode;
import dev.onelimit.velocityannouces.model.PluginConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class AnnounceCommand implements SimpleCommand {
    private final VelocityAnnoucesPlugin plugin;
    private final AnnouncementService announcementService;
    private final MiniMessage miniMessage;

    public AnnounceCommand(VelocityAnnoucesPlugin plugin, AnnouncementService announcementService) {
        this.plugin = plugin;
        this.announcementService = announcementService;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        PluginConfig config = plugin.currentConfig();
        boolean shouldCheckPermission = config.commandRequirePermission() && !config.commandPermission().isBlank();
        if (shouldCheckPermission && !source.hasPermission(config.commandPermission())) {
            source.sendMessage(msg("<red>You do not have permission."));
            return;
        }

        if (args.length == 0) {
            sendUsage(source);
            return;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                plugin.reload();
                source.sendMessage(msg("<green>VelocityAnnouces reloaded."));
            }
            case "send" -> handleSend(source, args);
            default -> sendUsage(source);
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0) {
            return List.of("reload", "send");
        }

        if (args.length == 1) {
            return filter(List.of("reload", "send"), args[0]);
        }

        if (args.length == 2 && "send".equalsIgnoreCase(args[0])) {
            return filter(List.of("chat", "actionbar", "title", "bossbar"), args[1]);
        }

        return Collections.emptyList();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(suggest(invocation));
    }

    private void handleSend(CommandSource source, String[] args) {
        if (args.length < 3) {
            source.sendMessage(msg("<yellow>Usage:</yellow> /vannounce send <chat|actionbar|title|bossbar> <message>"));
            source.sendMessage(msg("<yellow>Title format:</yellow> use <gray>|</gray> between title and subtitle."));
            return;
        }

        AnnounceMode mode = AnnounceMode.fromString(args[1]);
        String payload = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        switch (mode) {
            case CHAT -> announcementService.broadcastChat(payload);
            case ACTIONBAR -> announcementService.broadcastActionbar(payload);
            case BOSSBAR -> announcementService.broadcastBossbar(payload, 1.0f, "blue", "progress", 5);
            case TITLE -> {
                String[] split = payload.split("\\|", 2);
                String title = split.length > 0 ? split[0].trim() : payload;
                String subtitle = split.length > 1 ? split[1].trim() : "";
                announcementService.broadcastTitle(title, subtitle, 300, 2000, 400);
            }
        }

        source.sendMessage(msg("<green>Announcement sent.</green>"));
    }

    private void sendUsage(CommandSource source) {
        source.sendMessage(msg("<yellow>/vannounce reload"));
        source.sendMessage(msg("<yellow>/vannounce send <chat|actionbar|title|bossbar> <message>"));
    }

    private Component msg(String mm) {
        return miniMessage.deserialize(mm);
    }

    private List<String> filter(List<String> all, String prefix) {
        String lower = prefix.toLowerCase();
        return all.stream().filter(entry -> entry.startsWith(lower)).toList();
    }
}
