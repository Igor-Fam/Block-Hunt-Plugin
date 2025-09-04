package com.igor.blockhunt;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import net.kyori.adventure.key.Keyed;

import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Minigame {
    
    private final BlockHuntPlugin plugin;

    private MainCommand mainCommand;
    private Set<Player> escondedores = new HashSet<>();
    private Set<Player> procuradores = new HashSet<>();
    
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

    private final Location spawnEscondedor = new Location(Bukkit.getWorld("world"), 219.0, 73, -580.0);
    private final Location localEsperaProcurador = new Location(Bukkit.getWorld("world"), 223.5, 77, -484.5);

    private final int tempoEsperaProcurador = 35;
    private final int tempoMinigame = 900;

    BossBar bossBarAtiva;

    Minigame(MainCommand mainCommand, BlockHuntPlugin plugin) {
        this.mainCommand = mainCommand;
        this.plugin = plugin;
    }

    public void start() {
        //Limpa as barras anteriores
        java.util.Iterator<KeyedBossBar> bossBarIterator = Bukkit.getBossBars();
        while (bossBarIterator.hasNext()) {
            KeyedBossBar bossBar = bossBarIterator.next();
            bossBar.removeAll();      // Remove todos os jogadores da bossbar
            bossBar.setVisible(false); // Opcional: esconde a bossbar
        }

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        for(Player p : onlinePlayers){
            p.setGameMode(GameMode.ADVENTURE);
        }

        Team escondedoresTeam = scoreboard.getTeam("EscondedoresBH");
        Team procuradoresTeam = scoreboard.getTeam("Procuradores");
        if (escondedoresTeam == null) {
            escondedoresTeam = scoreboard.registerNewTeam("EscondedoresBH");
        }
        if (procuradoresTeam == null) {
            procuradoresTeam = scoreboard.registerNewTeam("Procuradores");
        }

        Collections.shuffle(onlinePlayers);
        Player procurador = onlinePlayers.get(0);
        procuradores.add(procurador);
        onlinePlayers.remove(0);
        procuradoresTeam.addEntry(procurador.getName());
        procurador.teleport(localEsperaProcurador);
        procurador.sendMessage("Você foi escolhido como Procurador!");
        giveArmor(procurador);

        for(Player p : onlinePlayers){
            escondedores.add(p);
            escondedoresTeam.addEntry(p.getName());
            p.teleport(spawnEscondedor);
            mainCommand.giveWand(p);
            p.sendMessage("Você é um Escondedor! Fuja do Procurador!");
        }

        criaTempoEspera();
    }

    private void giveArmor(Player player){
        // Exemplo de como dar uma armadura simples
        // Você pode personalizar a armadura conforme necessário
        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);

        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
        player.getInventory().addItem(sword);
    }

    private void criaTempoEspera(){
        BossBar bossbar = Bukkit.createBossBar("Tempo para o Procurador começar a procurar: " + tempoEsperaProcurador + " segundos", BarColor.RED, BarStyle.SOLID);
        bossBarAtiva = bossbar;
        
        bossbar.setProgress(1.0);
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(p);
        }
        final int[] tempoRestante = {tempoEsperaProcurador};
        final int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().runTaskTimer(
            plugin, 
            () -> {
                if (tempoRestante[0] <= 0) {
                    bossbar.removeAll();
                    bossbar.setVisible(false);
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    for(Player p : procuradores) {
                        p.teleport(spawnEscondedor);
                    }
                    criaTempoMinigame();
                    return;
                } else {
                    bossbar.setTitle("Tempo para Esconder");
                    bossbar.setProgress((double) tempoRestante[0] / tempoEsperaProcurador);
                }
                tempoRestante[0]--;
            }, 
        0, 20L).getTaskId();
    }

    private void criaTempoMinigame(){
        BossBar bossbar = Bukkit.createBossBar("Tempo restante", BarColor.GREEN, BarStyle.SOLID);
        bossBarAtiva = bossbar;
        bossbar.setProgress(1.0);

        for (Player p : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(p);
        }
        final int[] tempoRestante = {tempoMinigame};
        final int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().runTaskTimer(
            plugin, 
            () -> {
                if (tempoRestante[0] <= 0) {
                    bossbar.removeAll();
                    bossbar.setVisible(false);
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                } else {
                    bossbar.setTitle("Tempo restante");
                    bossbar.setProgress((double) tempoRestante[0] / tempoMinigame);
                }
                tempoRestante[0]--;
            },
        0, 20L).getTaskId();
    }

    public void stopMinigame(){
        bossBarAtiva.removeAll();
        bossBarAtiva.setVisible(false);
        escondedores.clear();
        procuradores.clear();
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        for(Player p : onlinePlayers){
            p.setGameMode(GameMode.SPECTATOR);
        }
    }

    public Set<Player> getEscondedores() {
        return escondedores;
    }

    public Set<Player> getProcuradores() {
        return procuradores;
    }
}
