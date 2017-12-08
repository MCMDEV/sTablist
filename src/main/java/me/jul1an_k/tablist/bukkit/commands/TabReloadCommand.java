package me.jul1an_k.tablist.bukkit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.jul1an_k.tablist.bukkit.Tablist;
import me.jul1an_k.tablist.bukkit.tabprefix.TabPrefix;

public class TabReloadCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
		if(!cs.hasPermission("sTablist.reload")) {
			cs.sendMessage("§4You don't have permission to use this command!");
			return true;
		}
		Tablist.getPlugin(Tablist.class).reloadConfig();
		
		Tablist.stopUpdate();
		Tablist.startUpdate();
		
		for(String group : TabPrefix.getGroupsFile().getYaml().getConfigurationSection("").getKeys(false)) {
			if(group.equalsIgnoreCase("GroupSort")) {
				continue;
			}
			
			TabPrefix.setupGroup(group, TabPrefix.getGroupsFile().getYaml().getString(group + ".Prefix"), TabPrefix.getGroupsFile().getYaml().getString(group + ".Suffix"));
		}
		
		cs.sendMessage("§aSuccessfully reloaded the Configuration File!");
		
		return true;
	}
	
}
