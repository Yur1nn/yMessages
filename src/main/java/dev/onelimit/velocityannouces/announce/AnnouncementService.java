package dev.onelimit.velocityannouces.announce;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.onelimit.velocityannouces.VelocityAnnoucesPlugin;
import dev.onelimit.velocityannouces.model.AnnounceMode;
import dev.onelimit.velocityannouces.model.AnnouncementTypeConfig;
import dev.onelimit.velocityannouces.model.PluginConfig;
import dev.onelimit.ycore.velocity.api.text.CoreTextRenderer;
import dev.onelimit.ycore.velocity.api.util.CorePlaceholders;
import dev.onelimit.ycore.velocity.api.util.CoreValueParsers;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

public final class AnnouncementService {
    private final VelocityAnnoucesPlugin plugin;
    private final ProxyServer server;
    private final CoreTextRenderer textRenderer;
    private final Random random;

    private ScheduledTask chatTask;
    private ScheduledTask actionbarTask;
    private ScheduledTask titleTask;
    private ScheduledTask bossbarTask;
    private ScheduledTask activeBossbarAnimation;
    private BossBar activeBossbar;
    private PluginConfig config;
    private int chatIndex;
    private int actionbarIndex;
    private int titleIndex;
    private int bossbarIndex;

    public AnnouncementService(VelocityAnnoucesPlugin plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
        this.textRenderer = new CoreTextRenderer();
        this.random = new Random();
        this.config = PluginConfig.defaults();
        this.chatIndex = 0;
        this.actionbarIndex = 0;
        this.titleIndex = 0;
        this.bossbarIndex = 0;
    }

    public void updateConfig(PluginConfig config) {
        this.config = config;
        shutdown();
        start();
    }

    public void start() {
        if (config.chatConfig().enabled()) {
            startScheduler(config.chatConfig(), "chat", this::broadcastChat);
        }
        if (config.actionbarConfig().enabled()) {
            startScheduler(config.actionbarConfig(), "actionbar", this::broadcastActionbar);
        }
        if (config.titleConfig().enabled()) {
            startScheduler(config.titleConfig(), "title", this::broadcastTitle);
        }
        if (config.bossbarConfig().enabled()) {
            startBossbarCycling();
        }
    }

    public void shutdown() {
        stopTask(chatTask);
        chatTask = null;
        stopTask(actionbarTask);
        actionbarTask = null;
        stopTask(titleTask);
        titleTask = null;
        stopTask(bossbarTask);
        bossbarTask = null;
        cancelActiveBossbar();
    }

    private void stopTask(ScheduledTask task) {
        if (task != null) {
            task.cancel();
        }
    }

    private void startScheduler(AnnouncementTypeConfig typeConfig, String typeName, Runnable action) {
        if (config.debug()) {
            plugin.logger().info("Starting {} announcements: interval={}s, randomSelection={}, messages={}", 
                typeName, typeConfig.intervalSeconds(), typeConfig.randomSelection(), typeConfig.messages().size());
        }

        long interval = Math.max(5L, typeConfig.intervalSeconds());
        ScheduledTask task = server.getScheduler()
            .buildTask(plugin, action)
            .delay(interval, TimeUnit.SECONDS)
            .repeat(interval, TimeUnit.SECONDS)
            .schedule();

        switch (typeName) {
            case "chat" -> chatTask = task;
            case "actionbar" -> actionbarTask = task;
            case "title" -> titleTask = task;
            case "bossbar" -> bossbarTask = task;
        }
    }

    private void broadcastChat() {
        String message = getCurrentMessage(config.chatConfig(), chatIndex++, AnnounceMode.CHAT);
        if (message.isEmpty()) {
            return;
        }
        
        Component rendered = render(message);
        for (Player player : server.getAllPlayers()) {
            player.sendMessage(rendered);
        }

        if (config.debug()) {
            plugin.logger().info("Broadcasted chat: {}", message);
        }
    }

    private void broadcastActionbar() {
        String message = getCurrentMessage(config.actionbarConfig(), actionbarIndex++, AnnounceMode.ACTIONBAR);
        if (message.isEmpty()) {
            return;
        }

        Component rendered = render(message);
        for (Player player : server.getAllPlayers()) {
            player.sendActionBar(rendered);
        }

        if (config.debug()) {
            plugin.logger().info("Broadcasted actionbar: {}", message);
        }
    }

    private void broadcastTitle() {
        String message = getCurrentMessage(config.titleConfig(), titleIndex++, AnnounceMode.TITLE);
        if (message.isEmpty()) {
            return;
        }

        String[] parts = message.split("\\|", 2);
        String title = parts[0].trim();
        String subtitle = parts.length > 1 ? parts[1].trim() : "";

        Component titleComponent = render(title);
        Component subtitleComponent = render(subtitle);

        Title.Times times = Title.Times.times(
            Duration.ofMillis(config.titleConfig().fadeInMs()),
            Duration.ofMillis(config.titleConfig().stayMs()),
            Duration.ofMillis(config.titleConfig().fadeOutMs())
        );

        Title titleObj = Title.title(titleComponent, subtitleComponent, times);
        for (Player player : server.getAllPlayers()) {
            player.showTitle(titleObj);
        }

        if (config.debug()) {
            plugin.logger().info("Broadcasted title: {} | {}", title, subtitle);
        }
    }

    public void broadcastEmergency(String message, AnnounceMode mode) {
        switch (mode) {
            case CHAT -> {
                Component rendered = render(message);
                for (Player player : server.getAllPlayers()) {
                    player.sendMessage(rendered);
                }
            }
            case ACTIONBAR -> {
                Component rendered = render(message);
                for (Player player : server.getAllPlayers()) {
                    player.sendActionBar(rendered);
                }
            }
            case TITLE -> {
                String[] parts = message.split("\\|", 2);
                Component title = render(parts[0]);
                Component subtitle = parts.length > 1 ? render(parts[1]) : Component.empty();
                Title.Times times = Title.Times.times(
                    Duration.ofMillis(300),
                    Duration.ofMillis(2000),
                    Duration.ofMillis(400)
                );
                Title titleObj = Title.title(title, subtitle, times);
                for (Player player : server.getAllPlayers()) {
                    player.showTitle(titleObj);
                }
            }
            case BOSSBAR -> {
                BossBar bossBar = BossBar.bossBar(
                    render(message),
                    1.0f,
                    BossBar.Color.BLUE,
                    BossBar.Overlay.PROGRESS
                );
                List<Player> players = List.copyOf(server.getAllPlayers());
                for (Player player : players) {
                    player.showBossBar(bossBar);
                }
                server.getScheduler()
                    .buildTask(plugin, () -> {
                        for (Player player : players) {
                            player.hideBossBar(bossBar);
                        }
                    })
                    .delay(5, TimeUnit.SECONDS)
                    .schedule();
            }
        }
    }

    private String getCurrentMessage(AnnouncementTypeConfig typeConfig, int index, AnnounceMode mode) {
        if (typeConfig.messages().isEmpty()) {
            return "";
        }

        int actualIndex;
        if (typeConfig.randomSelection()) {
            actualIndex = random.nextInt(typeConfig.messages().size());
            if (config.debug()) {
                plugin.logger().info("Selected random {} message index={}", mode.name().toLowerCase(), actualIndex);
            }
        } else {
            actualIndex = index % typeConfig.messages().size();
            if (config.debug()) {
                plugin.logger().info("Selected round-robin {} message index={}", mode.name().toLowerCase(), actualIndex);
            }
        }

        return typeConfig.messages().get(actualIndex);
    }

    private void startBossbarCycling() {
        cancelActiveBossbar();

        List<String> bossbarMessages = config.bossbarConfig().messages();
        if (bossbarMessages.isEmpty()) {
            return;
        }

        if (config.debug()) {
            plugin.logger().info("Starting bossbar cycling with {} entries", bossbarMessages.size());
        }

        if (!config.bossbarConfig().randomSelection()) {
            bossbarIndex = 0;
        }
        showNextBossbarInCycle();
    }

    private void showNextBossbarInCycle() {
        List<String> messages = config.bossbarConfig().messages();
        if (messages.isEmpty()) {
            return;
        }

        int currentIndex;
        if (config.bossbarConfig().randomSelection()) {
            currentIndex = random.nextInt(messages.size());
        } else {
            if (bossbarIndex >= messages.size()) {
                bossbarIndex = 0;
            }
            currentIndex = bossbarIndex;
            bossbarIndex++;
        }
        String message = messages.get(currentIndex);

        long totalSeconds = Math.max(1, config.bossbarConfig().intervalSeconds());

        BossBar bossBar = BossBar.bossBar(
            render(applyProgressToken(message, 0f)),
            0f,
            CoreValueParsers.parseEnum(BossBar.Color.class, config.bossbarConfig().defaultColor(), BossBar.Color.BLUE),
            CoreValueParsers.parseEnum(BossBar.Overlay.class, config.bossbarConfig().defaultOverlay(), BossBar.Overlay.PROGRESS)
        );

        activeBossbar = bossBar;
        List<Player> players = List.copyOf(server.getAllPlayers());
        for (Player player : players) {
            player.showBossBar(bossBar);
        }

        if (config.debug()) {
            plugin.logger().info("Showing bossbar {} of {}: {}", currentIndex + 1, messages.size(), message);
        }

        AtomicInteger elapsed = new AtomicInteger(0);
        int animationSpeed = Math.max(1, config.bossbarConfig().animationSpeed());
        long updateIntervalMs = 1000 / animationSpeed;

        activeBossbarAnimation = server.getScheduler()
            .buildTask(plugin, () -> {
                int passed = elapsed.incrementAndGet();
                float progress = CoreValueParsers.clamp((float) passed / (float) totalSeconds, 0f, 1f);

                bossBar.progress(progress);
                bossBar.name(render(applyProgressToken(message, progress)));

                if (passed >= totalSeconds) {
                    for (Player player : players) {
                        player.hideBossBar(bossBar);
                    }
                    if (activeBossbarAnimation != null) {
                        activeBossbarAnimation.cancel();
                        activeBossbarAnimation = null;
                    }
                    server.getScheduler()
                        .buildTask(plugin, this::showNextBossbarInCycle)
                        .delay(100, TimeUnit.MILLISECONDS)
                        .schedule();
                }
            })
            .repeat(updateIntervalMs, TimeUnit.MILLISECONDS)
            .schedule();
    }

    private Component render(String input) {
        return textRenderer.render(input);
    }

    private String applyProgressToken(String rawInput, float progress) {
        String safe = rawInput == null ? "" : rawInput;
        int percent = Math.round(CoreValueParsers.clamp(progress, 0f, 1f) * 100f);
        return CorePlaceholders.replaceExact(safe, Map.of(
            "<progress>", percent + "%",
            "{progress}", Integer.toString(percent)
        ));
    }

    private void cancelActiveBossbar() {
        if (activeBossbarAnimation != null) {
            activeBossbarAnimation.cancel();
            activeBossbarAnimation = null;
        }

        if (activeBossbar != null) {
            for (Player player : server.getAllPlayers()) {
                player.hideBossBar(activeBossbar);
            }
            activeBossbar = null;
        }
    }
}
