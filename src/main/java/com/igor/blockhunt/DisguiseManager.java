package com.igor.blockhunt;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
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

        // Disguise for others, invisible to self
        MiscDisguise disguise = new MiscDisguise(DisguiseType.FALLING_BLOCK, blockMaterial);
        DisguiseAPI.disguiseToAll(player, disguise);

        // Set helmet for self-view
        player.getInventory().setHelmet(new ItemStack(blockMaterial));

        disguisedPlayers.put(player.getUniqueId(), blockMaterial);
        player.sendMessage("§aVocê agora está disfarçado de " + blockMaterial.name() + "!");
        player.getInventory().addItem(new ItemStack(blockMaterial));
        startSolidifyTask(player);
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

        // Undisguise before turning solid
        DisguiseAPI.undisguiseToAll(player);
        player.getInventory().setHelmet(null); // Clear helmet before going into spectator

        Location loc = player.getLocation().getBlock().getLocation();
        // Snap to grid center
        loc.add(0.5, 0, 0.5);
        loc.setYaw(player.getLocation().getYaw());
        loc.setPitch(player.getLocation().getPitch());

        player.teleport(loc);

        Location blockLocation = loc.getBlock().getLocation();

        // Store original block
        solidBlocks.put(player.getUniqueId(), blockLocation.getBlock().getState());
        solidBlockLocations.put(blockLocation, player.getUniqueId());

        // Place the new block
        Block block = blockLocation.getBlock();
        block.setType(disguisedPlayers.get(player.getUniqueId()));
        BlockData bd = block.getBlockData();
        if (bd instanceof Directional) {
            ((Directional) bd).setFacing(player.getFacing());
            block.setBlockData(bd, false);
        }

        // Make player invisible and invulnerable
        player.setGameMode(GameMode.SPECTATOR);

        player.sendMessage("§2Você virou um bloco! §aFique parado para não ser descoberto.");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    public void Liquify(Player player){
        if (isSolid(player)) {
            BlockState originalState = solidBlocks.remove(player.getUniqueId());
            solidBlockLocations.remove(originalState.getLocation());
            originalState.update(true, false); // Revert to the original block
            player.setGameMode(GameMode.ADVENTURE); // Set back to default gamemode
            
            Material disguiseMaterial = disguisedPlayers.get(player.getUniqueId());
            if (disguiseMaterial != null) {
                MiscDisguise disguise = new MiscDisguise(DisguiseType.FALLING_BLOCK, disguiseMaterial);
                DisguiseAPI.disguiseToAll(player, disguise);
            }
        }
    }

    public void revert(Player player) {
        Material disguiseMaterial = disguisedPlayers.get(player.getUniqueId());
        if (disguiseMaterial != null) {
            player.getInventory().removeItem(new ItemStack(disguiseMaterial));
        }
        cancelSolidifyTask(player);

        if (isSolid(player)) {
            BlockState originalState = solidBlocks.remove(player.getUniqueId());
            solidBlockLocations.remove(originalState.getLocation());
            originalState.update(true, false); // Revert to the original block
            player.setGameMode(plugin.getServer().getDefaultGameMode()); // Set back to default gamemode
        }

        // This part handles players who are disguised but not solid
        if (disguisedPlayers.containsKey(player.getUniqueId())) {
            DisguiseAPI.undisguiseToAll(player);
            player.getInventory().setHelmet(null); // Clear helmet
            disguisedPlayers.remove(player.getUniqueId());
            player.sendMessage("§eVocê não está mais disfarçado.");
        }
    }

    public void resetAll() {
        // Use a copy of the keySet to prevent ConcurrentModificationException
        for (UUID playerId : new HashSet<>(disguisedPlayers.keySet())) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                revert(player);
            }
        }
        // Also revert any solid players
        for (UUID playerId : new HashSet<>(solidBlocks.keySet())) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                revert(player);
            }
        }
    }
}
