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

        // We only care about players who are disguised but not yet solid.
        // If a player is solid, they are in spectator mode and can move freely.
        if (!disguiseManager.isDisguised(player) || disguiseManager.isSolid(player)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        // Check if the player has moved a significant distance (i.e., moved a block)
        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            // If a disguised (but not solid) player moves, reset their solidify timer.
            disguiseManager.cancelSolidifyTask(player);
            disguiseManager.startSolidifyTask(player);
        }
    }
}
