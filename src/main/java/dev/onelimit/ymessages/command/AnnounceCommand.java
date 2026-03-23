package dev.onelimit.ymessages.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import dev.onelimit.ymessages.YMessagesPlugin;
import dev.onelimit.ymessages.announce.AnnouncementService;
import dev.onelimit.ymessages.model.AnnounceMode;
import dev.onelimit.ymessages.model.PluginConfig;
import dev.onelimit.ycore.velocity.api.text.CoreTextRenderer;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class AnnounceCommand implements SimpleCommand {
    private final YMessagesPlugin plugin;
    private final AnnouncementService announcementService;
    private final CoreTextRenderer textRenderer;

    public AnnounceCommand(YMessagesPlugin plugin, AnnouncementService announcementService) {
        this.plugin = plugin;
        this.announcementService = announcementService;
        this.textRenderer = new CoreTextRenderer();
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
                source.sendMessage(msg("<green>yMessages reloaded."));
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

            announcementService.broadcastEmergency(payload, mode);

        source.sendMessage(msg("<green>Announcement sent.</green>"));
    }

    private void sendUsage(CommandSource source) {
        source.sendMessage(msg("<yellow>/vannounce reload"));
        source.sendMessage(msg("<yellow>/vannounce send <chat|actionbar|title|bossbar> <message>"));
    }

    private Component msg(String mm) {
        return textRenderer.render(mm);
    }

    private List<String> filter(List<String> all, String prefix) {
        String lower = prefix.toLowerCase();
        return all.stream().filter(entry -> entry.startsWith(lower)).toList();
    }
}

