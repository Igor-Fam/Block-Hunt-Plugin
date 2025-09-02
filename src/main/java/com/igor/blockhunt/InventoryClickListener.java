package com.igor.blockhunt;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    private final DisguiseManager disguiseManager;

    public InventoryClickListener(DisguiseManager disguiseManager) {
        this.disguiseManager = disguiseManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(PlayerInteractListener.GUI_TITLE)) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                Material blockMaterial = clickedItem.getType();
                if (blockMaterial.isBlock()) {
                    disguiseManager.disguise(player, blockMaterial);
                    player.closeInventory();
                }
            }
        }
    }
}
