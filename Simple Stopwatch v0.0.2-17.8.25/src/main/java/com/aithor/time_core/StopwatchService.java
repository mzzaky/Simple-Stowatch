package com.aithor.time_core;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StopwatchService {
    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final Map<UUID, StopwatchData> stopwatches = new ConcurrentHashMap<>();

    public StopwatchService(JavaPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public boolean has(UUID id) {
        return stopwatches.containsKey(id);
    }

    public StopwatchData get(UUID id) {
        return stopwatches.get(id);
    }

    public StopwatchData start(UUID id) {
        return stopwatches.compute(id, (k, v) -> {
            if (v == null) return new StopwatchData();
            if (!v.isRunning()) {
                v.reset();
            }
            return v;
        });
    }

    public StopwatchData stop(UUID id) {
        StopwatchData data = stopwatches.get(id);
        if (data != null) {
            data.stop();
            stopwatches.remove(id);
        }
        return data;
    }

    public StopwatchData pause(UUID id) {
        StopwatchData data = stopwatches.get(id);
        if (data != null) data.pause();
        return data;
    }

    public StopwatchData resume(UUID id) {
        StopwatchData data = stopwatches.get(id);
        if (data != null) data.resume();
        return data;
    }

    public StopwatchData reset(UUID id) {
        StopwatchData data = stopwatches.get(id);
        if (data != null) data.reset();
        return data;
    }

    public long elapsed(UUID id) {
        StopwatchData data = stopwatches.get(id);
        return data == null ? 0 : data.getElapsedTime();
    }

    public long recordLap(UUID id) {
        StopwatchData data = stopwatches.get(id);
        return data == null ? 0 : data.recordLap();
    }

    public List<Long> getLaps(UUID id) {
        StopwatchData data = stopwatches.get(id);
        return data == null ? null : data.getLaps();
    }

    public void remove(UUID id) {
        stopwatches.remove(id);
    }

    public void shutdown() {
        stopwatches.clear();
    }

    public BukkitTask startUpdater(long intervalTicks) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                updateActiveStopwatches();
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);
    }

    private void updateActiveStopwatches() {
        for (Map.Entry<UUID, StopwatchData> e : stopwatches.entrySet()) {
            Player p = Bukkit.getPlayer(e.getKey());
            if (p != null && p.isOnline() && e.getValue().isRunning()) {
                String text = plugin.getConfig().getString("messages.action-bar-format", "%s");
                text = String.format(text, formatTime(e.getValue().getElapsedTime()));
                // Send to action bar to avoid chat spam; include color codes if present
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));
            }
        }
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
}