package com.igor.blockhunt;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

public class BlockBreakListener implements Listener {

    private final DisguiseManager disguiseManager;

    public BlockBreakListener(DisguiseManager disguiseManager) {
        this.disguiseManager = disguiseManager;
    }

    @EventHandler
    public void OnBlockDamage(BlockDamageEvent event) {
        Location blockLocation = event.getBlock().getLocation();

        Player disguisedPlayer = disguiseManager.getPlayerFromSolidBlock(blockLocation);

        if (disguisedPlayer != null) {
            // Cancel the block break event to prevent the block from dropping
            event.setCancelled(true);

            // Revert the player's disguise
            disguiseManager.revert(disguisedPlayer);

            // Notify the player who broke the block
            Player breaker = event.getPlayer();
            breaker.sendMessage("§aVocê encontrou um jogador disfarçado!");
            breaker.playSound(breaker.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);

            // Notify the player who was discovered
            disguisedPlayer.sendMessage("§cVocê foi descoberto!");
        }
    }
}
