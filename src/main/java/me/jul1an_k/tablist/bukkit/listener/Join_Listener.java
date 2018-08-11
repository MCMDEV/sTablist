package me.jul1an_k.tablist.bukkit.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.jul1an_k.tablist.bukkit.Tablist;
import me.jul1an_k.tablist.api.bukkit.sTablistAPI;
import me.jul1an_k.tablist.bukkit.tabprefix.TabPrefix;

public class Join_Listener implements Listener {
	
	private final Tablist instance = Tablist.getPlugin(Tablist.class);
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		
		boolean header = instance.getConfig().getBoolean("Header.use");
		boolean footer = instance.getConfig().getBoolean("Footer.use");
		
		if(header && footer) {
			sTablistAPI.getImpl().sendTabList(p, instance.getConfig().getString("Header.text"), instance.getConfig().getString("Footer.text"));
		} else if(header) {
			sTablistAPI.getImpl().sendTabList(p, instance.getConfig().getString("Header.text"), null);
		} else if(footer) {
			sTablistAPI.getImpl().sendTabList(p, null, instance.getConfig().getString("Footer.text"));
		}
		
		if(instance.getConfig().getBoolean("Join.Title.use")) {
			sTablistAPI.getImpl().sendTitle(p, instance.getConfig().getString("Join.Title.text"), instance.getConfig().getString("Join.Title.subtext"), instance.getConfig().getInt("Join.Title.fadein"), instance.getConfig().getInt("Join.Title.stay"), instance.getConfig().getInt("Join.Title.fadeout"), false, false);
		}
		
		if(instance.getConfig().getBoolean("Join.Actionbar.use")) {
			sTablistAPI.getImpl().sendActionBar(p, instance.getConfig().getString("Join.Actionbar.text"));
		}
		
		// if(Tablist.sbcfg.YAML.getBoolean("Enable")) {
		// for(String group :
		// TabPrefix.getGroupsFile().getYaml().getConfigurationSection("").getKeys(false))
		// {
		// if(group.equalsIgnoreCase("GroupSort")) {
		// continue;
		// }
		//
		// TabPrefix.setupGroup(group,
		// TabPrefix.getGroupsFile().getYaml().getString(group + ".Prefix"),
		// TabPrefix.getGroupsFile().getYaml().getString(group + ".Suffix"), p);
		// }
		// }

		TabPrefix.getImpl().loadNameTag(p);
		
		if(Tablist.getPlugin(Tablist.class).getConfig().getBoolean("UseExternalScoreboard")) {
			for(String group : TabPrefix.getGroupsFile().getYaml().getConfigurationSection("").getKeys(false)) {
				if(group.equalsIgnoreCase("GroupSort")) {
					continue;
				}
				
				TabPrefix.getImpl().setupGroup(group, TabPrefix.getGroupsFile().getYaml().getString(group + ".Prefix"), TabPrefix.getGroupsFile().getYaml().getString(group + ".Suffix"), p);
			}
			
			Bukkit.getScheduler().runTaskLater(Tablist.getPlugin(Tablist.class), () -> {
					for(String group : TabPrefix.getGroupsFile().getYaml().getConfigurationSection("").getKeys(false)) {
						if(group.equalsIgnoreCase("GroupSort")) {
							continue;
						}
						
						TabPrefix.getImpl().setupGroup(group, TabPrefix.getGroupsFile().getYaml().getString(group + ".Prefix"), TabPrefix.getGroupsFile().getYaml().getString(group + ".Suffix"), p);
					}
			}, 3 * 20);
		}
	}
	
}
