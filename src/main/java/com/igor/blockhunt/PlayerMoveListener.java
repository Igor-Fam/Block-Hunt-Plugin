package com.igor.blockhunt;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final DisguiseManager disguiseManager;

    public PlayerMoveListener(DisguiseManager disguiseManager) {
        this.disguiseManager = disguiseManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!disguiseManager.isDisguised(player)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if(player.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
            if(from.getBlockY() != to.getBlockY()) {
                event.setCancelled(true);
                return;
            }  
        }

        // Check if the player has moved a significant distance (i.e., moved a block)
        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            // If a disguised (but not solid) player moves, reset their solidify timer.
            disguiseManager.cancelSolidifyTask(player);
            disguiseManager.startSolidifyTask(player);
            if(disguiseManager.isSolid(player)){
                disguiseManager.revert(player);
            }
        }
    }
}
