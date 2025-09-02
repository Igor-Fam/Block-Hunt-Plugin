package com.igor.blockhunt;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BlockHuntPlugin extends JavaPlugin {

    private DisguiseManager disguiseManager;

    @Override
    public void onEnable() {
        // Save the default config.yml if it doesn't exist
        saveDefaultConfig();

        // Check for LibsDisguises
        if (!getServer().getPluginManager().isPluginEnabled("LibsDisguises")) {
            getLogger().severe("### LibsDisguises não foi encontrado! ###");
            getLogger().severe("### O BlockHunt precisa dele para funcionar. ###");
            getLogger().severe("### Desabilitando o plugin. ###");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize the manager
        this.disguiseManager = new DisguiseManager(this);

        // Register command
        MainCommand mainCommand = new MainCommand(this, this.disguiseManager);
        getCommand("blockhunt").setExecutor(mainCommand);
        getCommand("blockhunt").setTabCompleter(mainCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(disguiseManager), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(disguiseManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(disguiseManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(disguiseManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(disguiseManager), this);
        getServer().getPluginManager().registerEvents(new ConfigGUIListener(this), this);

        getLogger().info("BlockHunt Plugin foi ativado com sucesso!");
    }

    @Override
    public void onDisable() {
        if (disguiseManager != null) {
            disguiseManager.resetAll();
        }
        getLogger().info("BlockHunt Plugin foi desativado.");
    }

    public DisguiseManager getDisguiseManager() {
        return disguiseManager;
    }

    public List<Material> getSelectableBlocks() {
        List<String> materialNames = getConfig().getStringList("selectable-blocks");
        if (materialNames == null || materialNames.isEmpty()) {
            // Provide a default list if the config is empty
            return new ArrayList<>(Arrays.asList(Material.STONE, Material.DIRT, Material.OAK_LOG, Material.CRAFTING_TABLE));
        }

        return materialNames.stream()
                .map(Material::matchMaterial)
                .filter(m -> m != null && m.isBlock())
                .collect(Collectors.toList());
    }

    public void saveSelectableBlocks(List<Material> blocks) {
        List<String> materialNames = blocks.stream()
                .map(Material::name)
                .collect(Collectors.toList());
        getConfig().set("selectable-blocks", materialNames);
        saveConfig();
    }
}