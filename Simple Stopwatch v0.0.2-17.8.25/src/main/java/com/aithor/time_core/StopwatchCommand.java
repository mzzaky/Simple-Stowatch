package com.aithor.time_core;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class StopwatchCommand implements CommandExecutor {
    private final StopwatchService service;
    private final JavaPlugin plugin;

    public StopwatchCommand(StopwatchService service, JavaPlugin plugin) {
        this.service = service;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMsg("no-console", "Hanya player yang bisa menggunakan command ini!"));
            return true;
        }

        Player p = (Player) sender;
        UUID id = p.getUniqueId();

        String permission = plugin.getConfig().getString("permission", "stopwatch.use");
        if (!p.hasPermission(permission)) {
            p.sendMessage(getMsgColored("no-permission", ChatColor.RED + "Kamu tidak memiliki izin untuk menggunakan stopwatch."));
            return true;
        }

        if (args.length == 0) {
            showHelp(p);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "start":
                service.start(id);
                p.sendMessage(getMsgColored("started", ChatColor.GREEN + "⏱ Stopwatch dimulai!"));
                break;

            case "stop":
                StopwatchData stopped = service.stop(id);
                if (stopped == null) {
                    p.sendMessage(getMsgColored("not-started", ChatColor.RED + "⏱ Kamu belum memulai stopwatch!"));
                } else {
                    p.sendMessage(String.format(getMsg("stopped", "⏱ Stopwatch dihentikan: %s"), ChatColor.AQUA + formatTime(stopped.getElapsedTime())));
                }
                break;

            case "pause":
                StopwatchData dataPause = service.get(id);
                if (dataPause == null) {
                    p.sendMessage(getMsgColored("not-started", ChatColor.RED + "⏱ Kamu belum memulai stopwatch!"));
                } else if (!dataPause.isRunning()) {
                    p.sendMessage(getMsgColored("already-paused", ChatColor.YELLOW + "⏱ Stopwatch sudah dipause!"));
                } else {
                    service.pause(id);
                    p.sendMessage(String.format(getMsg("paused", "⏱ Stopwatch dipause pada: %s"), ChatColor.AQUA + formatTime(service.elapsed(id))));
                }
                break;

            case "resume":
                StopwatchData dataResume = service.get(id);
                if (dataResume == null) {
                    p.sendMessage(getMsgColored("not-started", ChatColor.RED + "⏱ Kamu belum memulai stopwatch!"));
                } else if (dataResume.isRunning()) {
                    p.sendMessage(getMsgColored("already-running", ChatColor.YELLOW + "⏱ Stopwatch sudah berjalan!"));
                } else {
                    service.resume(id);
                    p.sendMessage(getMsgColored("resumed", ChatColor.GREEN + "⏱ Stopwatch dilanjutkan!"));
                }
                break;

            case "reset":
                if (!service.has(id)) {
                    p.sendMessage(getMsgColored("not-started", ChatColor.RED + "⏱ Kamu belum memulai stopwatch!"));
                } else {
                    service.reset(id);
                    p.sendMessage(getMsgColored("reset", ChatColor.GOLD + "⏱ Stopwatch direset!"));
                }
                break;

            case "time":
                if (!service.has(id)) {
                    p.sendMessage(getMsgColored("not-started", ChatColor.RED + "⏱ Kamu belum memulai stopwatch!"));
                } else {
                    StopwatchData t = service.get(id);
                    String status = t.isRunning() ? ChatColor.GREEN + "Berjalan" : ChatColor.YELLOW + "Dipause";
                    p.sendMessage(ChatColor.GOLD + "⏱ Waktu: " + ChatColor.AQUA + formatTime(t.getElapsedTime()) + ChatColor.GRAY + " [" + status + ChatColor.GRAY + "]");
                }
                break;

            case "lap":
                if (!service.has(id)) {
                    p.sendMessage(getMsgColored("not-started", ChatColor.RED + "⏱ Kamu belum memulai stopwatch!"));
                } else {
                    long lap = service.recordLap(id);
                    StopwatchData d = service.get(id);
                    p.sendMessage(ChatColor.LIGHT_PURPLE + "⏱ Lap " + d.getLaps().size() + ": " + ChatColor.AQUA + formatTime(lap) + ChatColor.GRAY + " (Total: " + ChatColor.AQUA + formatTime(d.getElapsedTime()) + ChatColor.GRAY + ")");
                }
                break;

            default:
                showHelp(p);
        }

        return true;
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

    private String getMsg(String key, String fallback) {
        return plugin.getConfig().getString("messages." + key, fallback);
    }

    private String getMsgColored(String key, String fallback) {
        // config may include color codes using Minecraft § or &; we assume color-coded strings are stored already
        return getMsg(key, fallback);
    }
}