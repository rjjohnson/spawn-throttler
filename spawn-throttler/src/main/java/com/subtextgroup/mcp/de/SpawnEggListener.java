package com.subtextgroup.mcp.de;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;


public class SpawnEggListener implements Listener {
    SpawnThrottler throttler;
    
    private int maxSpawnsPerInterval;
    private long intervalMs;
    
    public SpawnEggListener(SpawnThrottler throttler, int maxSpawnsPerInterval, long intervalMs) {
        this.throttler = throttler;
        this.maxSpawnsPerInterval = maxSpawnsPerInterval;
        this.intervalMs = intervalMs;
    }

    

    Map<UUID, MonitoredSpawn> spawnFrequencies = new HashMap<>();
    
    private boolean tooFrequent(MonitoredSpawn spawn) {
        if (spawn.lastSpawn == null) {
            return false;
        }
        int spawnCount = 0;
        MonitoredSpawn lastSpawn = spawn;
        while (lastSpawn != null) {
            if (spawn.timestamp - lastSpawn.timestamp < intervalMs) {
                spawnCount++;
            } else {
                return false;
            }

            if (spawnCount > maxSpawnsPerInterval) {
                return true;
            }

            lastSpawn = lastSpawn.lastSpawn;
        }
        return false;
    }

    
    @EventHandler(ignoreCancelled = true)
    public void onThrowEgg(PlayerInteractEvent event) {

        if(event.getItem().getType() != Material.MONSTER_EGG || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        MonitoredSpawn lastSpawn = spawnFrequencies.get(id);
        MonitoredSpawn thisSpawn = new MonitoredSpawn();
        thisSpawn.timestamp = System.currentTimeMillis();
        if (lastSpawn != null) {
            thisSpawn.lastSpawn = lastSpawn;
        }
        spawnFrequencies.put(id, thisSpawn);

        if (tooFrequent(thisSpawn)) {
            player.sendMessage("Chill out! You may only spawn " + maxSpawnsPerInterval + " every " + (intervalMs / 1000) + " seconds.");
            event.setCancelled(true);
        }

    }
    public void clear() {
        spawnFrequencies.clear();
    }
}
