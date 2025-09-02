package com.igor.blockhunt;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GiveWandCommand implements CommandExecutor, TabCompleter {

    private final BlockHuntPlugin plugin;

    public GiveWandCommand(BlockHuntPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("wand")) {
            Player player = (Player) sender;

            String materialName = plugin.getConfig().getString("wand-item", "BLAZE_ROD");
            Material wandMaterial = Material.matchMaterial(materialName);
            if (wandMaterial == null) {
                player.sendMessage("§cMaterial do item inválido no config.yml!");
                return true;
            }

            ItemStack wand = new ItemStack(wandMaterial, 1);
            ItemMeta meta = wand.getItemMeta();

            if (meta != null) {
                String wandName = plugin.getConfig().getString("wand-name", "&6Block Hunt Wand");
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', wandName));

                List<String> lore = plugin.getConfig().getStringList("wand-lore").stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList());
                meta.setLore(lore);
                wand.setItemMeta(meta);
            }

            player.getInventory().addItem(wand);
            player.sendMessage("§aVocê recebeu a Varinha Block Hunt!");
            return true;
        }

        sender.sendMessage("§eUso: /" + label + " wand");
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("wand");
        }
        return new ArrayList<>();
    }
}
