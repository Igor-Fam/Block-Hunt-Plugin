package com.igor.blockhunt;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class restartCommand implements CommandExecutor {

	private final DisguiseManager disguiseManager;

	public restartCommand(DisguiseManager disguiseManager) {
		this.disguiseManager = disguiseManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Permite que apenas operadores usem o comando
		if (!sender.isOp()) {
			sender.sendMessage("§cVocê não tem permissão para usar este comando.");
			return true;
		}
		disguiseManager.ResetAll();
		sender.sendMessage("§aTodos os jogadores foram destransformados!");
		return true;
	}
}
