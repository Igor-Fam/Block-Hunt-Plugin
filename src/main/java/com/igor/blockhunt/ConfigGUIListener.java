package com.igor.blockhunt;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ConfigGUIListener implements Listener {

    private final BlockHuntPlugin plugin;

    public ConfigGUIListener(BlockHuntPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(ConfigGUI.GUI_TITLE)) {
            return;
        }

        List<Material> newBlocks = new ArrayList<>();
        for (ItemStack item : event.getInventory().getContents()) {
            if (item != null && item.getType().isBlock()) {
                newBlocks.add(item.getType());
            }
        }

        plugin.saveSelectableBlocks(newBlocks);
        Player player = (Player) event.getPlayer();
        player.sendMessage("§aLista de blocos selecionáveis foi salva!");
    }
}
