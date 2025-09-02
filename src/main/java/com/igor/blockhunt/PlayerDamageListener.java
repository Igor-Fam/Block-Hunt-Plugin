package com.igor.blockhunt;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamageListener implements Listener {

    private final DisguiseManager disguiseManager;

    public PlayerDamageListener(DisguiseManager disguiseManager) {
        this.disguiseManager = disguiseManager;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player damagedPlayer = (Player) event.getEntity();

            // Check if the player is disguised
            if (disguiseManager.isDisguised(damagedPlayer)) {
                // Remove the disguise
                disguiseManager.revert(damagedPlayer);
                
                // Optional: Send a message to the attacker
                if (event.getDamager() instanceof Player) {
                    Player attacker = (Player) event.getDamager();
                    attacker.sendMessage("§eVocê revelou um jogador disfarçado!");
                    attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                }
            }
        }
    }
}