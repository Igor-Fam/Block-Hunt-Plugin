package com.igor.blockhunt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ConfigGUI {

    public static final String GUI_TITLE = "§8Configurar Blocos";
    private final BlockHuntPlugin plugin;

    public ConfigGUI(BlockHuntPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        List<Material> blocks = plugin.getSelectableBlocks();
        // Create an inventory with enough space (multiples of 9, max 54)
        int size = (int) Math.ceil(blocks.size() / 9.0) * 9;
        size = Math.max(9, Math.min(54, size));
        Inventory gui = Bukkit.createInventory(null, size, GUI_TITLE);

        for (Material block : blocks) {
            gui.addItem(new ItemStack(block));
        }

        player.openInventory(gui);
    }
}
