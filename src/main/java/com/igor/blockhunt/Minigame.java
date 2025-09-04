package com.igor.blockhunt;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.Location;
import org.bukkit.boss.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Minigame {

    private MainCommand mainCommand;
    private Set<Player> escondedores = new HashSet<>();
    private Set<Player> procuradores = new HashSet<>();

    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

    private final Location spawnEscondedor = new Location(Bukkit.getWorld("world"), 219.0, 73, -580.0);
    private final Location localEsperaProcurador = new Location(Bukkit.getWorld("world"), 223.5, 77, -484.5);

    private final int tempoEsperaProcurador = 35;
    private final int tempoMinigame = 900;

    Minigame(MainCommand mainCommand){
        this.mainCommand = mainCommand;
    }

    public void start() {
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

        for(Player p : onlinePlayers){
            escondedores.add(p);
            escondedoresTeam.addEntry(p.getName());
            p.teleport(spawnEscondedor);
            mainCommand.giveWand(p);
            p.sendMessage("Você é um Escondedor! Fuja do Procurador!");
        }

        criaTempoEspera();

        Bukkit.getScheduler().runTaskLater(
            Bukkit.getPluginManager().getPlugin("BlockHuntPlugin"),
            () -> {
                for(Player p : procuradores) {
                    p.teleport(spawnEscondedor);
                }
                criaTempoMinigame();
            },
            tempoEsperaProcurador * 20L
        );
    }

    private void criaTempoEspera(){
        BossBar bossbar = Bukkit.createBossBar("Tempo para o Procurador começar a procurar: " + tempoEsperaProcurador + " segundos", BarColor.RED, BarStyle.SOLID);
        bossbar.setProgress(1.0);
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(p);
        }
        final int[] tempoRestante = {tempoEsperaProcurador};
        final int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().runTaskTimer(
            Bukkit.getPluginManager().getPlugin("BlockHuntPlugin"), 
            () -> {
                if (tempoRestante[0] <= 0) {
                    bossbar.removeAll();
                    bossbar.setVisible(false);
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                } else {
                    bossbar.setTitle("Tempo para o Procurador começar a procurar: " + tempoRestante[0] + " segundos");
                    bossbar.setProgress((double) tempoRestante[0] / tempoEsperaProcurador);
                }
                tempoRestante[0]--;
            }, 
        0, 20L).getTaskId();
    }

    private void criaTempoMinigame(){
        BossBar bossbar = Bukkit.createBossBar("Tempo restante", BarColor.GREEN, BarStyle.SOLID);
        bossbar.setProgress(1.0);

        for (Player p : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(p);
        }
        final int[] tempoRestante = {tempoMinigame};
        final int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().runTaskTimer(
            Bukkit.getPluginManager().getPlugin("BlockHuntPlugin"), 
            () -> {
                if (tempoRestante[0] <= 0) {
                    bossbar.removeAll();
                    bossbar.setVisible(false);
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                } else {
                    bossbar.setTitle("Tempo restante: " + tempoRestante[0] + " segundos");
                    bossbar.setProgress((double) tempoRestante[0] / tempoMinigame);
                }
                tempoRestante[0]--;
            },
        0, 20L).getTaskId();
    }

    public Set<Player> getEscondedores() {
        return escondedores;
    }

    public Set<Player> getProcuradores() {
        return procuradores;
    }
}
