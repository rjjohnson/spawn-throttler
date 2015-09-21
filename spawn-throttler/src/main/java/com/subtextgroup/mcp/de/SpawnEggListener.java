package com.subtextgroup.mcp.de;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;


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

            if (spawnCount >= maxSpawnsPerInterval) {
                return true;
            }

            lastSpawn = lastSpawn.lastSpawn;
        }
        return false;
    }

    private boolean isMonsterEgg(ItemStack itemStack) {
    	if(itemStack == null) {
    		return false;
    	}
    	Material itemType = itemStack.getType();
    	return itemType == Material.MONSTER_EGG;
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntityWithMonsterEgg(PlayerInteractEntityEvent event) {
    	Entity target = event.getRightClicked();
    	if(target == null || !(target instanceof LivingEntity)) {
    		return;
    	}
    	Player player = event.getPlayer();
    	if(player == null) {
    		return;
    	}
    	ItemStack itemStackInHand = player.getItemInHand();
    	if(!isMonsterEgg(itemStackInHand)) {
    		return;
    	}
    	handleSpawn(player, event);
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onInteractWithMonsterEgg(PlayerInteractEvent event) {
    	if(event == null) {
    		return;
    	}
    	ItemStack itemStack = event.getItem();
    	if(itemStack == null) {
    		return;
    	}
    	
    	if(!isMonsterEgg(itemStack) || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
    		return;
    	}

    	handleSpawn(event.getPlayer(), event);

    }
    private void handleSpawn(Player player, Cancellable cancellable) {
    	if(player == null) {
    		return;
    	}
        UUID id = player.getUniqueId();
        MonitoredSpawn lastSpawn = spawnFrequencies.get(id);
        MonitoredSpawn thisSpawn = new MonitoredSpawn();
        thisSpawn.timestamp = System.currentTimeMillis();
        if (lastSpawn != null) {
            thisSpawn.lastSpawn = lastSpawn;
        }
        spawnFrequencies.put(id, thisSpawn);

        if (tooFrequent(thisSpawn)) {
            player.sendMessage("Chill out! You may only spawn " + maxSpawnsPerInterval + " mob every " + (intervalMs / 1000) + " seconds.");
            cancellable.setCancelled(true);
        }
    }
    public void clear() {
        spawnFrequencies.clear();
    }
}
