package com.aithor.time_core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Stopwatch extends JavaPlugin {

    private final Map<UUID, StopwatchData> stopwatches = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("SimpleStopwatch plugin telah aktif!");

        // Start timer untuk update display
        new BukkitRunnable() {
            @Override
            public void run() {
                updateActiveStopwatches();
            }
        }.runTaskTimer(this, 0L, 20L); // Update setiap detik
    }

    @Override
    public void onDisable() {
        getLogger().info("SimpleStopwatch plugin telah dimatikan!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Hanya player yang bisa menggunakan command ini!");
            return true;
        }

        Player p = (Player) sender;
        UUID id = p.getUniqueId();

        if (cmd.getName().equalsIgnoreCase("stopwatch") || cmd.getName().equalsIgnoreCase("sw")) {
            if (args.length == 0) {
                showHelp(p);
                return true;
            }

            String sub = args[0].toLowerCase();

            switch (sub) {
                case "start":
                    startStopwatch(p, id);
                    break;

                case "stop":
                    stopStopwatch(p, id);
                    break;

                case "pause":
                    pauseStopwatch(p, id);
                    break;

                case "resume":
                    resumeStopwatch(p, id);
                    break;

                case "reset":
                    resetStopwatch(p, id);
                    break;

                case "time":
                    showTime(p, id);
                    break;

                case "lap":
                    recordLap(p, id);
                    break;

                default:
                    showHelp(p);
            }
            return true;
        }
        return false;
    }

    private void startStopwatch(Player p, UUID id) {
        if (stopwatches.containsKey(id) && stopwatches.get(id).isRunning) {
            p.sendMessage(ChatColor.YELLOW + "⏱ Stopwatch sudah berjalan!");
            return;
        }

        stopwatches.put(id, new StopwatchData());
        p.sendMessage(ChatColor.GREEN + "⏱ Stopwatch dimulai!");
    }

    private void stopStopwatch(Player p, UUID id) {
        if (!stopwatches.containsKey(id)) {
            p.sendMessage(ChatColor.RED + "⏱ Kamu belum memulai stopwatch!");
            return;
        }

        StopwatchData data = stopwatches.get(id);
        data.stop();
        p.sendMessage(ChatColor.RED + "⏱ Stopwatch dihentikan: " + ChatColor.AQUA + formatTime(data.getElapsedTime()));
        stopwatches.remove(id);
    }

    private void pauseStopwatch(Player p, UUID id) {
        if (!stopwatches.containsKey(id)) {
            p.sendMessage(ChatColor.RED + "⏱ Kamu belum memulai stopwatch!");
            return;
        }

        StopwatchData data = stopwatches.get(id);
        if (!data.isRunning) {
            p.sendMessage(ChatColor.YELLOW + "⏱ Stopwatch sudah dipause!");
            return;
        }

        data.pause();
        p.sendMessage(ChatColor.YELLOW + "⏱ Stopwatch dipause pada: " + ChatColor.AQUA + formatTime(data.getElapsedTime()));
    }

    private void resumeStopwatch(Player p, UUID id) {
        if (!stopwatches.containsKey(id)) {
            p.sendMessage(ChatColor.RED + "⏱ Kamu belum memulai stopwatch!");
            return;
        }

        StopwatchData data = stopwatches.get(id);
        if (data.isRunning) {
            p.sendMessage(ChatColor.YELLOW + "⏱ Stopwatch sudah berjalan!");
            return;
        }

        data.resume();
        p.sendMessage(ChatColor.GREEN + "⏱ Stopwatch dilanjutkan!");
    }

    private void resetStopwatch(Player p, UUID id) {
        if (!stopwatches.containsKey(id)) {
            p.sendMessage(ChatColor.RED + "⏱ Kamu belum memulai stopwatch!");
            return;
        }

        stopwatches.get(id).reset();
        p.sendMessage(ChatColor.GOLD + "⏱ Stopwatch direset!");
    }

    private void showTime(Player p, UUID id) {
        if (!stopwatches.containsKey(id)) {
            p.sendMessage(ChatColor.RED + "⏱ Kamu belum memulai stopwatch!");
            return;
        }

        StopwatchData data = stopwatches.get(id);
        String status = data.isRunning ? ChatColor.GREEN + "Berjalan" : ChatColor.YELLOW + "Dipause";
        p.sendMessage(ChatColor.GOLD + "⏱ Waktu: " + ChatColor.AQUA + formatTime(data.getElapsedTime()) +
                ChatColor.GRAY + " [" + status + ChatColor.GRAY + "]");
    }

    private void recordLap(Player p, UUID id) {
        if (!stopwatches.containsKey(id)) {
            p.sendMessage(ChatColor.RED + "⏱ Kamu belum memulai stopwatch!");
            return;
        }

        StopwatchData data = stopwatches.get(id);
        long lapTime = data.recordLap();
        p.sendMessage(ChatColor.LIGHT_PURPLE + "⏱ Lap " + data.laps.size() + ": " +
                ChatColor.AQUA + formatTime(lapTime) + ChatColor.GRAY + " (Total: " +
                ChatColor.AQUA + formatTime(data.getElapsedTime()) + ChatColor.GRAY + ")");
    }

    private void showHelp(Player p) {
        p.sendMessage(ChatColor.GOLD + "=== SimpleStopwatch Commands ===");
        p.sendMessage(ChatColor.YELLOW + "/sw start" + ChatColor.GRAY + " - Mulai stopwatch");
        p.sendMessage(ChatColor.YELLOW + "/sw stop" + ChatColor.GRAY + " - Hentikan stopwatch");
        p.sendMessage(ChatColor.YELLOW + "/sw pause" + ChatColor.GRAY + " - Pause stopwatch");
        p.sendMessage(ChatColor.YELLOW + "/sw resume" + ChatColor.GRAY + " - Lanjutkan stopwatch");
        p.sendMessage(ChatColor.YELLOW + "/sw reset" + ChatColor.GRAY + " - Reset stopwatch");
        p.sendMessage(ChatColor.YELLOW + "/sw time" + ChatColor.GRAY + " - Lihat waktu saat ini");
        p.sendMessage(ChatColor.YELLOW + "/sw lap" + ChatColor.GRAY + " - Catat lap time");
    }

    private void updateActiveStopwatches() {
        for (Map.Entry<UUID, StopwatchData> entry : stopwatches.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p != null && p.isOnline() && entry.getValue().isRunning) {
                p.sendMessage(ChatColor.GOLD + "⏱ " + ChatColor.AQUA + formatTime(entry.getValue().getElapsedTime()));
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

    // Inner class untuk data stopwatch
    private static class StopwatchData {
        private long startTime;
        private long pausedTime;
        private long totalPausedDuration;
        private boolean isRunning;
        private final java.util.List<Long> laps;

        public StopwatchData() {
            this.startTime = System.currentTimeMillis();
            this.totalPausedDuration = 0;
            this.isRunning = true;
            this.laps = new java.util.ArrayList<>();
        }

        public long getElapsedTime() {
            if (isRunning) {
                return System.currentTimeMillis() - startTime - totalPausedDuration;
            } else {
                return pausedTime - startTime - totalPausedDuration;
            }
        }

        public void pause() {
            if (isRunning) {
                pausedTime = System.currentTimeMillis();
                isRunning = false;
            }
        }

        public void resume() {
            if (!isRunning) {
                totalPausedDuration += System.currentTimeMillis() - pausedTime;
                isRunning = true;
            }
        }

        public void stop() {
            if (isRunning) {
                pausedTime = System.currentTimeMillis();
            }
            isRunning = false;
        }

        public void reset() {
            startTime = System.currentTimeMillis();
            totalPausedDuration = 0;
            pausedTime = 0;
            laps.clear();
        }

        public long recordLap() {
            long lapTime = getElapsedTime();
            laps.add(lapTime);
            return lapTime;
        }
    }
}