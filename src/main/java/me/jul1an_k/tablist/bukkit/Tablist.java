package me.jul1an_k.tablist.bukkit;

import me.jul1an_k.tablist.api.bukkit.sTablistAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.jul1an_k.tablist.bukkit.commands.TablistCommand;
import me.jul1an_k.tablist.bukkit.listener.Join_Listener;
import me.jul1an_k.tablist.bukkit.metrics.Metrics;
import me.jul1an_k.tablist.bukkit.tabprefix.TabPrefix;
import me.jul1an_k.tablist.bukkit.update.FileUpdate;
import me.jul1an_k.tablist.bukkit.variables.VariableManager;

public class Tablist extends JavaPlugin {
	
	public void onEnable() {
		long start = System.currentTimeMillis();

		sTablistAPI.setupTablistAPI();
		TabPrefix.setupTabPrefix();
		
		saveDefaultConfig();
		// saveResource("scoreboard.yml", false);
		
		loadConfig();
		
		if(getConfig().getBoolean("EnableAutoUpdater")) {
			FileUpdate updater = new FileUpdate();
			
			if(updater.check()) {
				return;
			}
			
			startPluginUpdate();
		}
		
		if(!getConfig().getString("ConfigVersion").equals(getDescription().getVersion())) {
			System.err.println("[sTablist] Found outdated Configuration! (Your Version: " + getConfig().getString("ConfigVersion") + " | Newest Version: " + getDescription().getVersion() + ")");
			System.err.println("[sTablist] Use /stablist regenConfig to regenerate the Configuration.");
		}
		
		for(String group : TabPrefix.getGroupsFile().getYaml().getConfigurationSection("").getKeys(false)) {
			if(group.equalsIgnoreCase("GroupSort")) {
				continue;
			}
			
			TabPrefix.getImpl().setupGroup(group, TabPrefix.getGroupsFile().getYaml().getString(group + ".Prefix"), TabPrefix.getGroupsFile().getYaml().getString(group + ".Suffix"));
		}
		
		Bukkit.getPluginManager().registerEvents(new Join_Listener(), this);
		
		if(Tablist.getPlugin(Tablist.class).getConfig().getBoolean("EnablePvPStats")) {
			Bukkit.getPluginManager().registerEvents(new VariableManager.PvPVariables(), this);
		}

		this.getCommand("update").setExecutor(new FileUpdate());
		this.getCommand("stablist").setExecutor(new TablistCommand());
		this.getCommand("stablist").setTabCompleter(new TablistCommand());
		startUpdate();
		startTagUpdate();
		
		new Metrics(this);
		
		long stop = System.currentTimeMillis();
		
		System.out.println("[sTablist] Started in " + (stop - start) + " Millis!");
	}
	
	private void loadConfig() {
		saveDefaultConfig();
		
		FileConfiguration fc = getConfig();
		
		if(!fc.contains("SkipDisguised")) {
			fc.set("SkipDisguised", false);
		}
		
		saveConfig();
	}
	
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
	}

	private static void hidePlayer(Player p) {
		if(p.getGameMode() == GameMode.SPECTATOR) {
			for(Player all : Bukkit.getOnlinePlayers()) {
				if(all.canSee(p)) {
					all.hidePlayer(getPlugin(Tablist.class), p);
				}
			}
		} else {
			for(Player all : Bukkit.getOnlinePlayers()) {
				if(!all.canSee(p)) {
					all.showPlayer(getPlugin(Tablist.class), p);
				}
			}
		}
	}
	
	private static int updateTask = 0;
	
	public static void startUpdate() {
		int interval = Tablist.getPlugin(Tablist.class).getConfig().getInt("TabAutoUpdate") * 20;

		updateTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Tablist.getPlugin(Tablist.class), () -> {
				for(Player p : Bukkit.getOnlinePlayers()) {
					Tablist instance = Tablist.getPlugin(Tablist.class);
					
					if(instance.getConfig().getBoolean("HideGM3")) {
						hidePlayer(p);
					}
					
					boolean header = instance.getConfig().getBoolean("Header.use");
					boolean footer = instance.getConfig().getBoolean("Footer.use");
					
					if(header && footer) {
						sTablistAPI.getImpl().sendTabList(p, instance.getConfig().getString("Header.text"), instance.getConfig().getString("Footer.text"));
					} else if(header) {
						sTablistAPI.getImpl().sendTabList(p, instance.getConfig().getString("Header.text"), null);
					} else if(footer) {
						sTablistAPI.getImpl().sendTabList(p, null, instance.getConfig().getString("Footer.text"));
					}
				}
		}, interval, interval);

		System.out.println("[sTablist] Started TabUpdater. Interval set to " + interval + " second(s)");
	}
	
	public static void stopUpdate() {
		Bukkit.getScheduler().cancelTask(updateTask);
	}
	
	private int tagUpdateTask = 0;
	
	private void startTagUpdate() {
		int interval = Tablist.getPlugin(Tablist.class).getConfig().getInt("TagAutoUpdate") * 20;

		tagUpdateTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Tablist.getPlugin(Tablist.class), () -> {
				for(Player all : Bukkit.getOnlinePlayers()) {
					if(getConfig().getBoolean("SkipDisguised") && me.libraryaddict.disguise.DisguiseAPI.isDisguised(all)) continue;

					TabPrefix.getImpl().loadNameTag(all);
				}
		}, interval, interval);

		System.out.println("[sTablist] Started TagUpdater. Interval set to " + interval + " second(s)");
	}

	private void stopTagUpdate() {
		Bukkit.getScheduler().cancelTask(tagUpdateTask);
	}
	
	private void startPluginUpdate() {
		int interval = Tablist.getPlugin(Tablist.class).getConfig().getInt("PluginAutoUpdate") * 20;

		final FileUpdate updater = new FileUpdate();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Tablist.getPlugin(Tablist.class), updater::check, interval, interval);

		System.out.println("[sTablist] Started PluginUpdater. Interval set to " + interval + " second(s)");
	}
	
}
