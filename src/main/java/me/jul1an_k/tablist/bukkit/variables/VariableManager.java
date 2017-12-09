package me.jul1an_k.tablist.bukkit.variables;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.jul1an_k.tablist.bukkit.Tablist;
import me.jul1an_k.tablist.bukkit.sTablistAPI;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class VariableManager {
	
	private static Map<String, Scroller> scrollers = new HashMap<>();

	private static Economy economy;
	private static Permission permission;

	static {
		if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			Economy economy = null;
			RegisteredServiceProvider<Economy> economyProvider = Tablist.getPlugin(Tablist.class).getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

			if(economyProvider != null) economy = economyProvider.getProvider();

			Permission permission = null;
			RegisteredServiceProvider<Permission> permissionProvider = Tablist.getPlugin(Tablist.class).getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);

			if(permissionProvider != null) permission = permissionProvider.getProvider();
		}
	}
	
	public static String replace(String msg, Player p) {
		String newmsg = msg;
		
		newmsg = newmsg.replace("%player%", p.getName());
		newmsg = newmsg.replace("%displayname%", p.getDisplayName());
		newmsg = newmsg.replace("%online%", getOnlinePlayers().size() + "");
		newmsg = newmsg.replace("%max_online%", Bukkit.getMaxPlayers() + "");
		newmsg = newmsg.replace("%servername%", Bukkit.getServerName());
		newmsg = newmsg.replace("%deaths%", PvPVariables.getDeaths(p) + "");
		newmsg = newmsg.replace("%kills%", PvPVariables.getKills(p) + "");
		newmsg = newmsg.replace("%rdm_color%", Animation.getRandomColor() + "");
		newmsg = newmsg.replace("%x%", (int) p.getLocation().getX() + "");
		newmsg = newmsg.replace("%y%", (int) p.getLocation().getY() + "");
		newmsg = newmsg.replace("%z%", (int) p.getLocation().getZ() + "");
		newmsg = newmsg.replace("%ping%", sTablistAPI.getImpl().getPing(p) + "");
		newmsg = newmsg.replace("%world%", p.getWorld().getName());
		
		int staffs = 0;
		
		for(Player all : getOnlinePlayers()) {
			if(all.hasPermission("sTablist.Staff")) {
				staffs++;
			}
		}
		
		newmsg = newmsg.replace("%staff_online%", staffs + "");
			
		if(economy != null) {
			newmsg = newmsg.replace("%money%", economy.getBalance(p) + "");
		}
			
		if(permission != null) {
			boolean replace = true;

			if(permission.getName().equalsIgnoreCase("SuperPerms")) {
				newmsg = newmsg.replace("%rank%", "Incompatible Permission System");
				replace = false;
			}

			if(replace) newmsg = newmsg.replace("%rank%", permission.getPlayerGroups(p)[0]);
		}
		
		if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) newmsg = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, newmsg);
		
		newmsg = ChatColor.translateAlternateColorCodes('&', newmsg);
		
		return newmsg;
	}
	
	public static String replaceTab(String msg, Player p) {
		String newmsg = msg;
		
		newmsg = newmsg.replace("%player%", p.getName());
		newmsg = newmsg.replace("%displayname%", p.getDisplayName());
		newmsg = newmsg.replace("%online%", getOnlinePlayers().size() + "");
		newmsg = newmsg.replace("%max_online%", Bukkit.getMaxPlayers() + "");
		newmsg = newmsg.replace("%servername%", Bukkit.getServerName());
		newmsg = newmsg.replace("%deaths%", PvPVariables.getDeaths(p) + "");
		newmsg = newmsg.replace("%kills%", PvPVariables.getKills(p) + "");
		newmsg = newmsg.replace("%rdm_color%", Animation.getRandomColor() + "");
		newmsg = newmsg.replace("%x%", (int) p.getLocation().getX() + "");
		newmsg = newmsg.replace("%y%", (int) p.getLocation().getY() + "");
		newmsg = newmsg.replace("%z%", (int) p.getLocation().getZ() + "");
		newmsg = newmsg.replace("%ping%", sTablistAPI.getImpl().getPing(p) + "");
		newmsg = newmsg.replace("%world%", p.getWorld().getName());
		
		int staffs = 0;
		
		for(Player all : getOnlinePlayers()) {
			if(all.hasPermission("sTablist.Staff")) {
				staffs++;
			}
		}
		
		newmsg = newmsg.replace("%staff_online%", staffs + "");
		
		if(newmsg.startsWith("%scroller%")) {
			if(!scrollers.containsKey(msg)) {
				String snewmsg = msg;
				snewmsg = snewmsg.replace("%player%", p.getName());
				snewmsg = snewmsg.replace("%displayname%", p.getDisplayName());
				snewmsg = snewmsg.replace("%online%", getOnlinePlayers().size() + "");
				snewmsg = snewmsg.replace("%max_online%", Bukkit.getMaxPlayers() + "");
				snewmsg = snewmsg.replace("%servername%", Bukkit.getServerName());
				snewmsg = snewmsg.replace("%deaths%", PvPVariables.getDeaths(p) + "");
				snewmsg = snewmsg.replace("%kills%", PvPVariables.getKills(p) + "");
				snewmsg = snewmsg.replace("%rdm_color%", Animation.getRandomColor() + "");
				snewmsg = snewmsg.replace("%scroller%", "");
				snewmsg = snewmsg.replace("%x%", (int) p.getLocation().getX() + "");
				snewmsg = snewmsg.replace("%y%", (int) p.getLocation().getY() + "");
				snewmsg = snewmsg.replace("%z%", (int) p.getLocation().getZ() + "");
				snewmsg = snewmsg.replace("%ping%", sTablistAPI.getImpl().getPing(p) + "");
				snewmsg = snewmsg.replace("%staff_online%", staffs + "");
				
				snewmsg = ChatColor.translateAlternateColorCodes('&', snewmsg);
				
				Scroller scroller = new Scroller(snewmsg, Tablist.getPlugin(Tablist.class).getConfig().getInt("TabAutoUpdate") * 20);
				scroller.start();
				scrollers.put(msg, scroller);
			}
		}
		
		newmsg = newmsg.replace("%scroller%", "");

		if(economy != null) {
			newmsg = newmsg.replace("%money%", economy.getBalance(p) + "");
		}

		if(permission != null) {
			boolean replace = true;

			if(permission.getName().equalsIgnoreCase("SuperPerms")) {
				newmsg = newmsg.replace("%rank%", "Incompatible Permission System");
				replace = false;
			}

			if(replace) newmsg = newmsg.replace("%rank%", permission.getPlayerGroups(p)[0]);
		}
		
		if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			newmsg = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, newmsg);
		}
		
		newmsg = ChatColor.translateAlternateColorCodes('&', newmsg);
		
		if(scrollers.containsKey(msg)) {
			return scrollers.get(msg).getCurrentText();
		}
		
		return newmsg;
	}
	
	public static Collection<? extends Player> getOnlinePlayers() {
		List<Player> onlinePlayers = new ArrayList<>();
		
		if(Tablist.getPlugin(Tablist.class).getConfig().getBoolean("HideGM3")) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.getGameMode().equals(GameMode.SPECTATOR))
					continue;
				
				onlinePlayers.add(p);
			}
		} else {
			return Bukkit.getOnlinePlayers();
		}
		
		return onlinePlayers;
	}
	
	public static class Scroller {
		
		private int restartTaskID;
		private int taskID;
		private int time;
		private int current;
		private String text;
		private String newtext;
		
		public Scroller(String text, int time) {
			this.text = text + " ";
			this.time = time;
		}
		
		public String getCurrentText() {
			return newtext;
		}
		
		public void start() {
			if(!Bukkit.getScheduler().isCurrentlyRunning(taskID)) {
				taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Tablist.getPlugin(Tablist.class), () -> {
						if(current < text.length() + 1) {
							newtext = Animation.sub(text, 0, current);
							current++;
						}
						
						if(current == text.length() + 1) {
							restart();
						}
				}, time, time);
			}
		}
		
		public void stop() {
			Bukkit.getScheduler().cancelTask(taskID);
		}
		
		public void restart() {
			newtext = "";
			current = 0;
			taskID = 0;
			if(!Bukkit.getScheduler().isCurrentlyRunning(restartTaskID)) {
				restartTaskID = Bukkit.getScheduler().runTaskLater(Tablist.getPlugin(Tablist.class), () -> {
						stop();
						start();
				}, 10).getTaskId();
			}
		}
		
	}
	
	public static class Animation {
		
		public static int current = 0;
		public static String text = "Test Text.";
		public static String newtext = "";
		public static boolean lock = false;
		
		public static void scrollTest() {
			Bukkit.getScheduler().scheduleSyncRepeatingTask(Tablist.getPlugin(Tablist.class), () -> {
					if(current < text.length() + 1) {
						if(!lock) {
							newtext = sub(text, 0, current);
							current++;
						} else {
							newtext = sub(text, current);
							current--;
						}
					} else if(current == text.length() + 1) {
						newtext = sub(text, current);
						lock = true;
						current--;
					}

					System.out.println(newtext);
			}, 20, 20);
		}
		
		public static String scroll(String text) {
			return sub(text, 0);
		}
		
		public static String sub(String text, int sub) {
			return text.substring(sub);
		}
		
		public static String sub(String text, int sub, int submax) {
			
			return text.substring(sub, submax);
		}
		
		public static ChatColor getRandomColor() {
			List<ChatColor> colors = new ArrayList<>();
			for(String s : Tablist.getPlugin(Tablist.class).getConfig().getStringList("RandomColors")) {
				s = s.replace("&", "");
				colors.add(ChatColor.getByChar(s));
			}
			
			if(colors.size() == 0) {
				return ChatColor.WHITE;
			}
			
			Random rdm = new Random();
			
			return colors.get(rdm.nextInt(colors.size()));
		}
		
	}
	
	public static class PvPVariables implements Listener {
		
		private static File f = new File("plugins/sTablist", "PvPStats.yml");
		private static FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
		
		public PvPVariables() {
			if(!f.exists()) {
				try {
					f.createNewFile();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		public static int getDeaths(Player p) {
			if(!fc.contains(p.getUniqueId() + ".Deaths")) {
				fc.set(p.getUniqueId() + ".Deaths", 0);
			}
			return fc.getInt(p.getUniqueId() + ".Deaths");
		}
		
		public static int getKills(Player p) {
			if(!fc.contains(p.getUniqueId() + ".Kills")) {
				fc.set(p.getUniqueId() + ".Kills", 0);
			}
			return fc.getInt(p.getUniqueId() + ".Kills");
		}
		
		public static void setDeaths(Player p, int Deaths) {
			fc.set(p.getUniqueId() + ".Deaths", Deaths);
			try {
				fc.save(f);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		public static void setKills(Player p, int Kills) {
			fc.set(p.getUniqueId() + ".Kills", Kills);
			try {
				fc.save(f);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		@EventHandler
		public void onDeath(PlayerDeathEvent e) {
			Player d = e.getEntity();
			Player k = d.getKiller();
			if(k != null) {
				int oldKills = getKills(k);
				setKills(k, oldKills + 1);
			}
			int oldDeaths = getDeaths(d);
			setDeaths(d, oldDeaths + 1);
			try {
				fc.save(f);
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
}
