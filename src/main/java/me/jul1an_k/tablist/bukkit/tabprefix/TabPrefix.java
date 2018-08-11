package me.jul1an_k.tablist.bukkit.tabprefix;

import me.jul1an_k.tablist.bukkit.api.impl.tabprefix.TabPrefix_TeamBased;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.jul1an_k.tablist.bukkit.config.ConfigFile;

public abstract class TabPrefix {

	private static TabPrefix implemantation;

	protected static final ConfigFile groupsFile = new ConfigFile("plugins/sTablist/Prefixes-And-Suffixes", "groups");
	protected static final ConfigFile playersFile = new ConfigFile("plugins/sTablist/Prefixes-And-Suffixes", "players");

	public abstract void setPrefix(Player p, String prefix);

	public abstract void setSuffix(Player p, String suffix);
	
	public abstract void unset(OfflinePlayer p);
	
	public abstract void setupGroup(String group, String prefix, String suffix);
	
	public abstract void setupGroup(String group, String prefix, String suffix, Player p);
	
	public abstract void setInGroup(Player p, String group);
	
	public abstract void loadNameTag(Player p);

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
