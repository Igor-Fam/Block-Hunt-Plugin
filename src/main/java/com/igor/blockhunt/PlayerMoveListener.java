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

        if (!disguiseManager.isDisguised(player) && !disguiseManager.isSolid(player)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        // Check if the player has moved a significant distance (i.e., moved a block)
        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            // If player was solid, revert them
            if (disguiseManager.isSolid(player)) {
                disguiseManager.revert(player);
                return; // Stop further processing
            }

            // If player is disguised but not solid, reset the solidify timer
            if (disguiseManager.isDisguised(player)) {
                disguiseManager.cancelSolidifyTask(player);
                disguiseManager.startSolidifyTask(player);
            }
        }
    }
}
