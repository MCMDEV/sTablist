package me.jul1an_k.tablist.bukkit.tabprefix;

import java.util.List;

import me.jul1an_k.tablist.global.api.impl.bukkit.tabprefix.TabPrefix_TeamBased;
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

public abstract class TabPrefix {

	private static TabPrefix implemantation;

	protected static ConfigFile groupsFile = new ConfigFile("plugins/sTablist/Prefixes-And-Suffixes", "groups");
	protected static ConfigFile playersFile = new ConfigFile("plugins/sTablist/Prefixes-And-Suffixes", "players");

	public abstract void setPrefix(Player p, String prefix);

	public abstract void setSuffix(Player p, String suffix);
	
	public abstract void unset(OfflinePlayer p);
	
	public abstract void setupGroup(String group, String prefix, String suffix);
	
	public abstract void setupGroup(String group, String prefix, String suffix, Player p);
	
	public abstract void setInGroup(Player p, String group);
	
	public abstract void loadNametag(Player p);

	public static TabPrefix getImpl() {
		return implemantation;
	}

	public static void setupTabPrefix() {
		implemantation = new TabPrefix_TeamBased();
	}

	public static ConfigFile getGroupsFile() {
		return groupsFile;
	}
}
