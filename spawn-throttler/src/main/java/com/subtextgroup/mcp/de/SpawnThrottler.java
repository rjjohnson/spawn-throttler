package com.subtextgroup.mcp.de;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnThrottler extends JavaPlugin {

    private static final int DEFAULT_MAX_SPAWNS_PER_INTERVAL = 3;
    private static final long DEFAULT_INTERVAL_MS = 10000;
    private int maxSpawnsPerInterval = DEFAULT_MAX_SPAWNS_PER_INTERVAL;
    private long intervalMs = DEFAULT_INTERVAL_MS;
    
    FileConfiguration config = null;
    
    SpawnEggListener listener = null;

    @Override
    public void onDisable() {
        listener.clear();
        HandlerList.unregisterAll(listener);
    }

    @Override
    public void onEnable() {
        config = getConfig();
        config.addDefault("max-spawns-per-interval", DEFAULT_MAX_SPAWNS_PER_INTERVAL);
        config.addDefault("interval-milliseconds", DEFAULT_INTERVAL_MS);
        this.maxSpawnsPerInterval = config.getInt("max-spawns-per-interval");
        this.intervalMs = config.getLong("interval-milliseconds");
        saveConfig();
        this.listener = new SpawnEggListener(this, maxSpawnsPerInterval, intervalMs);
        getServer().getPluginManager().registerEvents(listener, this);
        getServer().broadcastMessage("SpawnThrottler enabled!");
        
    }

   
}
