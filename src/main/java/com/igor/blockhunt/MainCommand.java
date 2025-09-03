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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final BlockHuntPlugin plugin;
    private final DisguiseManager disguiseManager;
    private final ConfigGUI configGUI;

    public MainCommand(BlockHuntPlugin plugin, DisguiseManager disguiseManager) {
        this.plugin = plugin;
        this.disguiseManager = disguiseManager;
        this.configGUI = new ConfigGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eUso: /" + label + " <wand|resetall|config>");
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "wand":
                return handleGiveWand(sender);
            case "resetdisguise":
                return handleResetAll(sender);
            case "config":
                return handleConfig(sender, args);
            case "resetconfig":
                return handleResetConfig(sender);
            default:
                sender.sendMessage("§cComando desconhecido. Uso: /" + label + " <wand|resetall|config>");
                return false;
        }
    }

    private boolean handleGiveWand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("blockhunt.givewand")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

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

    private boolean handleResetAll(CommandSender sender) {
        if (!sender.hasPermission("blockhunt.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        disguiseManager.resetAll();
        sender.sendMessage("§aTodos os disfarces foram resetados.");
        return true;
    }

    private boolean handleConfig(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }
        if (!sender.hasPermission("blockhunt.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }
        if (args.length < 2 || !args[1].equalsIgnoreCase("blocks")) {
            sender.sendMessage("§eUso: /blockhunt config blocks");
            return false;
        }

        configGUI.open((Player) sender);
        return true;
    }

    private boolean handleResetConfig(CommandSender sender) {
        if (!sender.hasPermission("blockhunt.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        List<Material> defaultBlocks = Arrays.asList(
        );

        plugin.saveSelectableBlocks(defaultBlocks);
        sender.sendMessage("§aConfiguração de blocos restaurada para o padrão.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>();
            if (sender.hasPermission("blockhunt.givewand")) {
                subcommands.add("wand");
            }
            if (sender.hasPermission("blockhunt.admin")) {
                subcommands.add("resetdisguise");
                subcommands.add("config");
                subcommands.add("resetconfig");
            }
            return subcommands;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("config")) {
            if (sender.hasPermission("blockhunt.admin")) {
                return Arrays.asList("blocks");
            }
        }
        return new ArrayList<>();
    }
}