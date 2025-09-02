package com.igor.blockhunt;

import org.bukkit.Location;
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
            disguisedPlayer.showTitle(
                net.kyori.adventure.title.Title.title(
                    net.kyori.adventure.text.Component.text("§cVocê foi descoberto!"),
                    net.kyori.adventure.text.Component.empty(),
                    net.kyori.adventure.title.Title.Times.times(
                        java.time.Duration.ofMillis(10 * 50),
                        java.time.Duration.ofMillis(40 * 50),
                        java.time.Duration.ofMillis(20 * 50)
                    )
                )
            );
        }
    }
}