package com.igor.blockhunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PlayerInteractListener implements Listener {

    private final BlockHuntPlugin plugin;
    public static final String GUI_TITLE = "Escolha um Bloco";

    public PlayerInteractListener(BlockHuntPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && item != null) {
            String wandMaterialName = plugin.getConfig().getString("wand-item", "BLAZE_ROD");
            Material wandMaterial = Material.matchMaterial(wandMaterialName);

            if (item.getType() == wandMaterial && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                String wandName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("wand-name", "&6Block Hunt Wand"));
                if (meta.hasDisplayName() && meta.getDisplayName().equals(wandName)) {
                    event.setCancelled(true);
                    openBlockSelectionGUI(player);
                }
            }
        }
    }

    private void openBlockSelectionGUI(Player player) {
        List<String> allowedBlocks = plugin.getConfig().getStringList("allowed-blocks");
        int inventorySize = (int) (Math.ceil(allowedBlocks.size() / 9.0) * 9);
        inventorySize = Math.max(9, Math.min(54, inventorySize)); // Ensure size is between 9 and 54

        Inventory gui = Bukkit.createInventory(null, inventorySize, GUI_TITLE);

        for (String blockName : allowedBlocks) {
            Material blockMaterial = Material.matchMaterial(blockName);
            if (blockMaterial != null && blockMaterial.isBlock()) {
                ItemStack blockItem = new ItemStack(blockMaterial);
                gui.addItem(blockItem);
            }
        }

        player.openInventory(gui);
    }
}
