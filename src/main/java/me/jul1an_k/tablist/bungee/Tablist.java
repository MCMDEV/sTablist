package me.jul1an_k.tablist.bungee;

import me.jul1an_k.tablist.bungee.config.TabConfig;
import me.jul1an_k.tablist.bungee.listener.Join_Listener;
import me.jul1an_k.tablist.bungee.metrics.Metrics;
import me.jul1an_k.tablist.bungee.update.FileUpdate;
import net.md_5.bungee.api.plugin.Plugin;

public class Tablist extends Plugin {
	
	private static Tablist instance;
	private TabConfig cfg;
	
	public void onEnable() {
		long start = System.currentTimeMillis();
		instance = this;
		loadCfg();
		getProxy().getPluginManager().registerListener(this, new Join_Listener());
		
		if(cfg.getYaml().getBoolean("Updater.Enable")) {
			FileUpdate fu = new FileUpdate();
			fu.check();
		}
		
		new Metrics(this);
		
		long stop = System.currentTimeMillis();
		
		System.out.println("[sTablist-Bungee] v" + getDescription().getVersion() + " started in " + (stop - start) + " Millis!");
	}
	
	private void loadCfg() {
		cfg = new TabConfig();
	}
	
	public static Tablist getInstance() {
		return instance;
	}
	
	public TabConfig getCfg() {
		return cfg;
	}
	
}
