package com.aithor.time_core;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final StopwatchService service;

    public PlayerListener(StopwatchService service) {
        this.service = service;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Remove stopwatch data to avoid memory leak; optionally persist instead
        service.remove(event.getPlayer().getUniqueId());
    }
}