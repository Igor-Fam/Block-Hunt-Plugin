package com.igor.blockhunt;

import org.bukkit.plugin.java.JavaPlugin;

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
        GiveWandCommand giveWandCommand = new GiveWandCommand(this);
        getCommand("blockhunt").setExecutor(giveWandCommand);
        getCommand("blockhunt").setTabCompleter(giveWandCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(disguiseManager), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(disguiseManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(disguiseManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(disguiseManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(disguiseManager), this);

        getLogger().info("BlockHunt Plugin foi ativado com sucesso!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BlockHunt Plugin foi desativado.");
    }

    public DisguiseManager getDisguiseManager() {
        return disguiseManager;
    }
}
