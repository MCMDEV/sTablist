package me.jul1an_k.tablist.bukkit.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.jul1an_k.tablist.bukkit.Tablist;
import me.jul1an_k.tablist.bukkit.sTablistAPI;

public class TabSetCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
		if(!cs.hasPermission("sTablist.setTab")) {
			cs.sendMessage("§4You don't have permission to use this command!");
			return true;
		}
		if(!(args.length >= 2)) {
			cs.sendMessage("§cUsage: /setTab Header|Footer <Header or Footer>");
			return true;
		}
		if(args[0].equalsIgnoreCase("Header")) {
			String header = "";
			for(int i = 1; i < args.length; i++) {
				header = header + " " + args[i];
			}
			Tablist.getPlugin(Tablist.class).getConfig().set("Header.Text", header);
			Tablist.getPlugin(Tablist.class).saveConfig();
		} else if(args[0].equalsIgnoreCase("Footer")) {
			String header = "";
			for(int i = 1; i < args.length; i++) {
				header = header + " " + args[i];
			}
			Tablist.getPlugin(Tablist.class).getConfig().set("Footer.Text", header);
			Tablist.getPlugin(Tablist.class).saveConfig();
		} else {
			cs.sendMessage("§cUsage: /setTab Header|Footer <Header or Footer>");
			return true;
		}
		cs.sendMessage("§aSuccessfully set the Tablist!");
		
		for(Player all : Bukkit.getOnlinePlayers()) {
			sTablistAPI.getImpl().sendTabList(all, Tablist.getPlugin(Tablist.class).getConfig().getString("Header"), Tablist.getPlugin(Tablist.class).getConfig().getString("Footer"));
		}
		return true;
	}
	
}
