package me.jul1an_k.tablist.bukkit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.jul1an_k.tablist.bukkit.commands.InfoCommand;
import me.jul1an_k.tablist.bukkit.commands.TabReloadCommand;
import me.jul1an_k.tablist.bukkit.commands.TabSetCommand;
import me.jul1an_k.tablist.bukkit.commands.TablistCommand;
import me.jul1an_k.tablist.bukkit.listener.Join_Listener;
import me.jul1an_k.tablist.bukkit.metrics.Metrics;
import me.jul1an_k.tablist.bukkit.scoreboard.STLScoreboard;
import me.jul1an_k.tablist.bukkit.scoreboard.ScoreboardConfig;
import me.jul1an_k.tablist.bukkit.tabprefix.TabPrefix;
import me.jul1an_k.tablist.bukkit.update.FileUpdate;
import me.jul1an_k.tablist.bukkit.variables.VariableManager;

public class Tablist extends JavaPlugin {
	
	public static ScoreboardConfig sbcfg;
	
	public void onEnable() {
		long start = System.currentTimeMillis();
		
		sTablistAPI.setupTablistAPI();
		
		saveDefaultConfig();
		// saveResource("scoreboard.yml", false);
		saveResource("Prefixes-And-Suffixes/groups.yml", false);
		
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
		
		// sbcfg = new ScoreboardConfig();
		//
		// if(!sbcfg.FILE.exists()) {
		// try {
		// sbcfg.FILE.createNewFile();
		// } catch(IOException e) {
		// e.printStackTrace();
		// }
		// }
		
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

		this.getCommand("stabreload").setExecutor(new TabReloadCommand());
		this.getCommand("settab").setExecutor(new TabSetCommand());
		this.getCommand("update").setExecutor(new FileUpdate());
		this.getCommand("stabinfo").setExecutor(new InfoCommand());
		this.getCommand("stablist").setExecutor(new TablistCommand());
		startUpdate();
		startTagUpdate();
		
		// if(sbcfg.YAML.getBoolean("Enable") == true) {
		// startScoreboardUpdate();
		// }
		
		new Metrics(this);
		
		long stop = System.currentTimeMillis();
		
		System.out.println("[sTablist] Started in " + (stop - start) + " Millis!");
	}
	
	private void loadConfig() {
		saveDefaultConfig();
		
		FileConfiguration fc = getConfig();
		if(!fc.contains("RandomColors")) {
			List<String> randomColors = new ArrayList<>();
			randomColors.add("&0");
			randomColors.add("&1");
			randomColors.add("&2");
			randomColors.add("&3");
			randomColors.add("&4");
			randomColors.add("&5");
			randomColors.add("&6");
			randomColors.add("&7");
			randomColors.add("&8");
			randomColors.add("&9");
			randomColors.add("&a");
			randomColors.add("&b");
			randomColors.add("&c");
			randomColors.add("&d");
			randomColors.add("&e");
			randomColors.add("&f");
			fc.set("RandomColors", randomColors);
		}
		
		if(!fc.contains("UseExternalScoreboard")) {
			fc.set("UseExternalScoreboard", false);
		}
		
		saveConfig();
	}
	
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
	}
	
	private static int UpdateTask = 0;
	
	public static void startUpdate() {
		long start = System.currentTimeMillis();
		UpdateTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Tablist.getPlugin(Tablist.class), () -> {
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
		}, Tablist.getPlugin(Tablist.class).getConfig().getInt("TabAutoUpdate") * 20, Tablist.getPlugin(Tablist.class).getConfig().getInt("TabAutoUpdate") * 20);
		long stop = System.currentTimeMillis();
		System.out.println("[sTablist] Tab Update Task was started in " + (stop - start) + " Millis!");
	}

	private static void hidePlayer(Player p) {
		if(p.getGameMode() == GameMode.SPECTATOR) {
			for(Player all : Bukkit.getOnlinePlayers()) {
				if(all.canSee(p)) {
					all.hidePlayer(p);
				}
			}
		} else {
			for(Player all : Bukkit.getOnlinePlayers()) {
				if(!all.canSee(p)) {
					all.showPlayer(p);
				}
			}
		}
	}
	
	public static void stopUpdate() {
		long start = System.currentTimeMillis();
		Bukkit.getScheduler().cancelTask(UpdateTask);
		long stop = System.currentTimeMillis();
		System.out.println("[sTablist] Tab Update Task was stopped in " + (stop - start) + " Millis!");
	}
	
	private int tagUpdateTask = 0;
	
	private void startTagUpdate() {
		long start = System.currentTimeMillis();

		tagUpdateTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Tablist.getPlugin(Tablist.class), () -> {
				for(Player all : Bukkit.getOnlinePlayers()) {
					TabPrefix.getImpl().loadNametag(all);
				}
		}, Tablist.getPlugin(Tablist.class).getConfig().getInt("TagAutoUpdate") * 20, Tablist.getPlugin(Tablist.class).getConfig().getInt("TagAutoUpdate") * 20);
		long stop = System.currentTimeMillis();

		System.out.println("[sTablist] Tag Update Task was started in " + (stop - start) + " Millis!");
	}

	private void stopTagUpdate() {
		long start = System.currentTimeMillis();
		Bukkit.getScheduler().cancelTask(tagUpdateTask);
		long stop = System.currentTimeMillis();
		System.out.println("[sTablist] Tag Update Task was stopped in " + (stop - start) + " Millis!");
	}
	
	private void startPluginUpdate() {
		long start = System.currentTimeMillis();

		Bukkit.getScheduler().scheduleSyncRepeatingTask(Tablist.getPlugin(Tablist.class), () -> {
				FileUpdate updater = new FileUpdate();
				updater.check();
		}, Tablist.getPlugin(Tablist.class).getConfig().getInt("PluginAutoUpdate") * 20, Tablist.getPlugin(Tablist.class).getConfig().getInt("PluginAutoUpdate") * 20);
		long stop = System.currentTimeMillis();

		System.out.println("[sTablist] Plugin Update Task was started in " + (stop - start) + " Millis!");
	}
	
	@SuppressWarnings("unused")
	private void startScoreboardUpdate() {
		long start = System.currentTimeMillis();

		Bukkit.getScheduler().scheduleSyncRepeatingTask(Tablist.getPlugin(Tablist.class), () -> {
				for(Player all : Bukkit.getOnlinePlayers()) {
					STLScoreboard.show(all);
				}
		}, sbcfg.YAML.getInt("UpdateTime"), sbcfg.YAML.getInt("UpdateTime"));
		long stop = System.currentTimeMillis();

		System.out.println("[sTablist] Scoreboard Update Task was started in " + (stop - start) + " Millis!");
	}
	
}
