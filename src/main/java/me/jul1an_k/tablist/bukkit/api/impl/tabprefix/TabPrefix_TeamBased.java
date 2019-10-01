package me.jul1an_k.tablist.bukkit.api.impl.tabprefix;

import java.util.List;

import me.jul1an_k.tablist.bukkit.tabprefix.TabPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.jul1an_k.tablist.bukkit.Tablist;
import me.jul1an_k.tablist.bukkit.variables.VariableManager;
import net.milkbowl.vault.permission.Permission;

public class TabPrefix_TeamBased extends TabPrefix {

	public void setPrefix(Player p, String prefix) {
		prefix = VariableManager.replace(prefix, p);
		Scoreboard board = Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard") ? p.getScoreboard() : Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = board.getTeam(p.getName()) == null ? board.registerNewTeam(p.getName()) : board.getTeam(p.getName());

		prefix = VariableManager.replace(prefix, p);

		team.setPrefix(prefix.length() > 15 ? prefix.substring(0, 16) : prefix);
		team.addEntry(p.getName());
		
		if(!Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard")) {
			for(Player all : Bukkit.getOnlinePlayers()) {
				all.setScoreboard(board);
			}
		}
		
		playersFile.getYaml().set(p.getUniqueId() + ".Prefix", ChatColor.translateAlternateColorCodes('&', prefix));
		
		playersFile.save();
	}

	public void setSuffix(Player p, String suffix) {
		suffix = VariableManager.replace(suffix, p);
		Scoreboard board = Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard") ? p.getScoreboard() : Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = board.getTeam(p.getName()) == null ? board.registerNewTeam(p.getName()) : board.getTeam(p.getName());

		suffix = VariableManager.replace(suffix, p);

		team.setSuffix(suffix.length() > 15 ? suffix.substring(0, 16) : suffix);
		team.addEntry(p.getName());
		
		if(!Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard")) {
			for(Player all : Bukkit.getOnlinePlayers()) {
				all.setScoreboard(board);
			}
		}
		
		playersFile.getYaml().set(p.getUniqueId() + ".Suffix", ChatColor.translateAlternateColorCodes('&', suffix));
		
		playersFile.save();
	}

	public void setColor(Player p, String color) {
		color = VariableManager.replace(color, p);
		Scoreboard board = Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard") ? p.getScoreboard() : Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = board.getTeam(p.getName()) == null ? board.registerNewTeam(p.getName()) : board.getTeam(p.getName());

		color = VariableManager.replace(color, p);

		team.setColor(ChatColor.valueOf(color));
		team.addEntry(p.getName());

		if(!Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard")) {
			for(Player all : Bukkit.getOnlinePlayers()) {
				all.setScoreboard(board);
			}
		}

		playersFile.getYaml().set(p.getUniqueId() + ".Color", color);

		playersFile.save();
	}
	
	public void unset(OfflinePlayer p) {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = board.getTeam(p.getName()) == null ? board.registerNewTeam(p.getName()) : board.getTeam(p.getName());

		team.setPrefix("");
		team.setSuffix("");

		if(!Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard")) {
			for(Player all : Bukkit.getOnlinePlayers()) {
				all.setScoreboard(board);
			}
		}
		
		playersFile.getYaml().set(p.getUniqueId() + ".Prefix", null);
		playersFile.getYaml().set(p.getUniqueId() + ".Suffix", null);
		
		playersFile.save();
	}
	
	public void setupGroup(String group, String prefix, String suffix, String color) {
		int sortID = getSortID(group);
		String groupName = "0" + (sortID < 10 ? ("0" + sortID) : sortID) + group;
		
		prefix = ChatColor.translateAlternateColorCodes('&', prefix);
		suffix = ChatColor.translateAlternateColorCodes('&', suffix);

		if(prefix.length() > 15) prefix = prefix.substring(0, 16);
		if(suffix.length() > 15) suffix = suffix.substring(0, 16);
		
		if(Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard")) {
			for(Player all : Bukkit.getOnlinePlayers()) {
				Scoreboard board = all.getScoreboard();
				Team team = board.getTeam(groupName) == null ? board.registerNewTeam(groupName) : board.getTeam(groupName);
				
				team.setPrefix(prefix);
				team.setSuffix(suffix);
				team.setColor(ChatColor.valueOf(color));
			}
		} else {
			Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
			Team team = board.getTeam(groupName) == null ? board.registerNewTeam(groupName) : board.getTeam(groupName);
			
			team.setPrefix(prefix);
			team.setSuffix(suffix);
			team.setColor(ChatColor.valueOf(color));
			for(Player all : Bukkit.getOnlinePlayers()) {
				all.setScoreboard(board);
			}
		}
	}
	
	public void setupGroup(String group, String prefix, String suffix, String color, Player p) {
		int sortID = getSortID(group);
		String groupName = "0" + (sortID < 10 ? ("0" + sortID) : sortID) + group;
		
		prefix = ChatColor.translateAlternateColorCodes('&', prefix);
		suffix = ChatColor.translateAlternateColorCodes('&', suffix);

		if(prefix.length() > 15) prefix = prefix.substring(0, 16);
		if(suffix.length() > 15) suffix = suffix.substring(0, 16);
		
		Scoreboard board = p.getScoreboard();
		Team team = board.getTeam(groupName) == null ? board.registerNewTeam(groupName) : board.getTeam(groupName);
		
		team.setPrefix(prefix);
		team.setSuffix(suffix);
		team.setColor(ChatColor.valueOf(color));
	}
	
	public void setInGroup(Player p, String group) {
		int sortID = getSortID(group);
		String groupName = "0" + (sortID < 10 ? ("0" + sortID) : sortID) + group;
		
		if(Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard")) {
			for(Player all : Bukkit.getOnlinePlayers()) {
				if(all.getScoreboard() == null)
					continue;
				Scoreboard board = all.getScoreboard();
				Team team = board.getTeam(groupName) == null ? board.registerNewTeam(groupName) : board.getTeam(groupName);
				
				team.addEntry(p.getName());
			}
		} else {
			Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
			Team team = board.getTeam(groupName) == null ? board.registerNewTeam(groupName) : board.getTeam(groupName);
			
			team.addEntry(p.getName());
			
			for(Player all : Bukkit.getOnlinePlayers()) {
				all.setScoreboard(board);
			}
		}
	}
	
	private int getSortID(String group) {
		List<String> groups = groupsFile.getYaml().getStringList("GroupSort");

		for(int i = 0; i < groups.size(); i++) {
			if(groups.get(i).equalsIgnoreCase(group)) {
				return i;
			}
		}

		return 99;
	}
	
	public void loadNameTag(Player p) {
		boolean block = false;
		if(playersFile.getYaml().contains(p.getUniqueId() + ".Prefix")) {
			setPrefix(p, playersFile.getYaml().getString(p.getUniqueId() + ".Prefix"));
			block = true;
		}
		
		if(playersFile.getYaml().contains(p.getUniqueId() + ".Suffix")) {
			setSuffix(p, playersFile.getYaml().getString(p.getUniqueId() + ".Suffix"));
			block = true;
		}

		if(playersFile.getYaml().contains(p.getUniqueId() + ".Color")) {
			setColor(p, playersFile.getYaml().getString(p.getUniqueId() + ".Color"));
			block = true;
		}
		
		if(block)
			return;
		
		if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			Permission permission = null;
			RegisteredServiceProvider<Permission> permissionProvider = Tablist.getPlugin(Tablist.class).getServer().getServicesManager().getRegistration(Permission.class);

			if(permissionProvider != null) permission = permissionProvider.getProvider();

			if(permission == null || permission.getName().equals("SuperPerms")) return;

			if((groupsFile.getYaml().contains(permission.getPlayerGroups(p)[0] + ".Prefix") | groupsFile.getYaml().contains(permission.getPlayerGroups(p)[0] + ".Suffix"))) setInGroup(p, permission.getPlayerGroups(p)[0]);
		}
	}

}
