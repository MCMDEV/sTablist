package me.jul1an_k.tablist.bukkit.tabprefix;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.jul1an_k.tablist.bukkit.Tablist;
import me.jul1an_k.tablist.bukkit.config.ConfigFile;
import me.jul1an_k.tablist.bukkit.variables.VariableManager;
import net.milkbowl.vault.permission.Permission;

public class TabPrefix implements Listener {
	
	private static ConfigFile groupsFile = new ConfigFile("plugins/sTablist/Prefixes-And-Suffixes", "groups");
	private static ConfigFile playersFile = new ConfigFile("plugins/sTablist/Prefixes-And-Suffixes", "players");
	
	@SuppressWarnings("deprecation")
	public static void setPrefix(Player p, String prefix) {
		prefix = VariableManager.replace(prefix, p);
		Scoreboard board = Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard") ? p.getScoreboard() : Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = board.getTeam(p.getName());
		if(team == null) {
			team = board.registerNewTeam(p.getName());
		}
		
		team.setPrefix(prefix);
		team.addPlayer(p);
		
		if(!Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard")) {
			for(Player all : Bukkit.getOnlinePlayers()) {
				all.setScoreboard(board);
			}
		}
		
		playersFile.getYaml().set(p.getUniqueId() + ".Prefix", prefix.replace("§", "&"));
		
		playersFile.save();
	}
	
	@SuppressWarnings("deprecation")
	public static void setSuffix(Player p, String suffix) {
		suffix = VariableManager.replace(suffix, p);
		Scoreboard board = Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard") ? p.getScoreboard() : Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = board.getTeam(p.getName());
		if(team == null) {
			team = board.registerNewTeam(p.getName());
		}
		
		team.setSuffix(suffix);
		team.addPlayer(p);
		
		if(!Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard")) {
			for(Player all : Bukkit.getOnlinePlayers()) {
				all.setScoreboard(board);
			}
		}
		
		playersFile.getYaml().set(p.getUniqueId() + ".Suffix", suffix.replace("§", "&"));
		
		playersFile.save();
	}
	
	public static void unset(OfflinePlayer p) {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = board.getTeam(p.getName());
		
		if(team != null) {
			board.getTeam(p.getName()).unregister();
		}
		
		if(!Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard")) {
			for(Player all : Bukkit.getOnlinePlayers()) {
				all.setScoreboard(board);
			}
		}
		
		playersFile.getYaml().set(p.getUniqueId() + ".Prefix", null);
		playersFile.getYaml().set(p.getUniqueId() + ".Suffix", null);
		
		playersFile.save();
	}
	
	public static void setupGroup(String group, String prefix, String suffix) {
		int sortID = getSortID(group);
		String groupName = "0" + (sortID < 10 ? ("0" + sortID) : sortID) + group;
		
		prefix = ChatColor.translateAlternateColorCodes('&', prefix);
		suffix = ChatColor.translateAlternateColorCodes('&', suffix);
		
		if(Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard")) {
			for(Player all : Bukkit.getOnlinePlayers()) {
				Scoreboard board = all.getScoreboard();
				Team team = board.getTeam(groupName) == null ? board.registerNewTeam(groupName) : board.getTeam(groupName);
				
				team.setPrefix(prefix);
				team.setSuffix(suffix);
			}
		} else {
			Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
			Team team = board.getTeam(groupName) == null ? board.registerNewTeam(groupName) : board.getTeam(groupName);
			
			team.setPrefix(prefix);
			team.setSuffix(suffix);
			for(Player all : Bukkit.getOnlinePlayers()) {
				all.setScoreboard(board);
			}
		}
	}
	
	public static void setupGroup(String group, String prefix, String suffix, Player p) {
		int sortID = getSortID(group);
		String groupName = "0" + (sortID < 10 ? ("0" + sortID) : sortID) + group;
		
		prefix = ChatColor.translateAlternateColorCodes('&', prefix);
		suffix = ChatColor.translateAlternateColorCodes('&', suffix);
		
		Scoreboard board = p.getScoreboard();
		Team team = board.getTeam(groupName) == null ? board.registerNewTeam(groupName) : board.getTeam(groupName);
		
		team.setPrefix(prefix);
		team.setSuffix(suffix);
	}
	
	private static void setInGroup(Player p, String group) {
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
	
	private static int getSortID(String group) {
		List<String> groups = groupsFile.getYaml().getStringList("GroupSort");
		for(int i = 0; i < groups.size(); i++) {
			if(groups.get(i).equalsIgnoreCase(group)) {
				return i;
			}
		}
		return 99;
	}
	
	public static void loadNametag(Player p) {
		boolean block = false;
		if(playersFile.getYaml().contains(p.getUniqueId() + ".Prefix")) {
			setPrefix(p, playersFile.getYaml().getString(p.getUniqueId() + ".Prefix"));
			block = true;
		}
		
		if(playersFile.getYaml().contains(p.getUniqueId() + ".Suffix")) {
			setSuffix(p, playersFile.getYaml().getString(p.getUniqueId() + ".Suffix"));
			block = true;
		}
		
		if(block) {
			return;
		}
		
		if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			Permission permission = null;
			RegisteredServiceProvider<Permission> permissionProvider = ((Tablist) Tablist.getPlugin(Tablist.class)).getServer().getServicesManager().getRegistration(Permission.class);
			if(permissionProvider != null) {
				permission = (Permission) permissionProvider.getProvider();
			}
			if(permission == null) {
				return;
			}
			if(permission.getName() == "SuperPerms") {
				return;
			}
			if((groupsFile.getYaml().contains(permission.getPlayerGroups(p)[0] + ".Prefix") | groupsFile.getYaml().contains(permission.getPlayerGroups(p)[0] + ".Suffix"))) {
				setInGroup(p, permission.getPlayerGroups(p)[0]);
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		loadNametag(p);
	}
	
	public static ConfigFile getPlayersFile() {
		return playersFile;
	}
	
	public static ConfigFile getGroupsFile() {
		return groupsFile;
	}
}
