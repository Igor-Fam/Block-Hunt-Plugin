package com.igor.blockhunt;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DisguiseManager {

    private final BlockHuntPlugin plugin;
    private final Map<UUID, Material> disguisedPlayers = new HashMap<>();
    private final Map<UUID, BukkitTask> solidifyTasks = new HashMap<>();
    private final Map<UUID, BlockState> solidBlocks = new HashMap<>();
    private final Map<Location, UUID> solidBlockLocations = new HashMap<>();

    public DisguiseManager(BlockHuntPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isDisguised(Player player) {
        return disguisedPlayers.containsKey(player.getUniqueId());
    }

    public boolean isSolid(Player player) {
        return solidBlocks.containsKey(player.getUniqueId());
    }

    public Player getPlayerFromSolidBlock(Location location) {
        UUID playerId = solidBlockLocations.get(location);
        if (playerId != null) {
            return plugin.getServer().getPlayer(playerId);
        }
        return null;
    }

    public void disguise(Player player, Material blockMaterial) {
        // Remove any previous disguise or solid state
        revert(player);

        MiscDisguise disguise = new MiscDisguise(DisguiseType.FALLING_BLOCK, blockMaterial);
        DisguiseAPI.disguiseToAll(player, disguise);
        disguisedPlayers.put(player.getUniqueId(), blockMaterial);
        player.sendMessage("§aVocê agora está disfarçado de " + blockMaterial.name() + "!");
    }

    public void startSolidifyTask(Player player) {
        if (!isDisguised(player) || solidifyTasks.containsKey(player.getUniqueId())) {
            return;
        }

        long delay = plugin.getConfig().getLong("solidify-delay-seconds", 5) * 20; // Ticks

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            solidify(player);
            solidifyTasks.remove(player.getUniqueId());
        }, delay);

        solidifyTasks.put(player.getUniqueId(), task);
    }

    public void cancelSolidifyTask(Player player) {
        if (solidifyTasks.containsKey(player.getUniqueId())) {
            solidifyTasks.get(player.getUniqueId()).cancel();
            solidifyTasks.remove(player.getUniqueId());
        }
    }

    private void solidify(Player player) {
        if (!isDisguised(player)) {
            return;
        }

        Location loc = player.getLocation().getBlock().getLocation();
        // Snap to grid center
        loc.add(0.5, 0, 0.5);
        loc.setYaw(0);
        loc.setPitch(0);

        player.teleport(loc);

        Location blockLocation = loc.getBlock().getLocation();

        // Store original block
        solidBlocks.put(player.getUniqueId(), blockLocation.getBlock().getState());
        solidBlockLocations.put(blockLocation, player.getUniqueId());

        // Place the new block
        blockLocation.getBlock().setType(disguisedPlayers.get(player.getUniqueId()));

        // Make player invisible and invulnerable
        player.setGameMode(GameMode.SPECTATOR);

        player.sendTitle("§2Você virou um bloco!", "§aFique parado para não ser descoberto.", 10, 70, 20);
    }

    public void revert(Player player) {
        cancelSolidifyTask(player);

        if (isSolid(player)) {
            BlockState originalState = solidBlocks.remove(player.getUniqueId());
            solidBlockLocations.remove(originalState.getLocation());
            originalState.update(true, false); // Revert to the original block
            player.setGameMode(plugin.getServer().getDefaultGameMode()); // Set back to default gamemode
        }

        if (isDisguised(player)) {
            DisguiseAPI.undisguiseToAll(player);
            disguisedPlayers.remove(player.getUniqueId());
            player.sendMessage("§eVocê não está mais disfarçado.");
        }
    }

    public void cleanup(Player player) {
        revert(player);
    }
}
