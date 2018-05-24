package me.jul1an_k.tablist.bukkit.update;

import static me.jul1an_k.tablist.api.HTTPApi.downloadFile;
import static me.jul1an_k.tablist.api.HTTPApi.readLine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.UnknownDependencyException;

import me.jul1an_k.tablist.bukkit.Tablist;

public class FileUpdate implements CommandExecutor {
	
	public boolean check() {
		try {
			String newVersion = readLine("http://mc.jumpy91.de/Jul1anUpdater/sTablistUpdate.txt");
			String oldVersion = Tablist.getPlugin(Tablist.class).getDescription().getVersion();
			System.out.println("[sTablist-AutoUpdater] Newest Version: " + newVersion + "! Current Version: " + oldVersion);
			if(!newVersion.equals(oldVersion)) {
				String changesString = readLine("http://mc.jumpy91.de/Jul1anUpdater/sTablistChanges.txt");
				System.out.println("[sTablist-AutoUpdater] New in this version: " + changesString);
				for(Player all : Bukkit.getOnlinePlayers()) {
					if(all.hasPermission("sTablist.Update")) {
						all.sendMessage("§aAn Update for sTablist is available!");
						all.sendMessage("§cCurrent Version: " + oldVersion);
						all.sendMessage("§2New Version: " + newVersion);
						all.sendMessage("§3New in this version: " + changesString);
						all.sendMessage("§aDownloading update...");
					}
				}
				downloadUpdate(Bukkit.getConsoleSender());
				for(Player all : Bukkit.getOnlinePlayers()) {
					if(all.hasPermission("sTablist.Update")) {
						all.sendMessage("§aDownloaded update!");
					}
				}
			} else {
				return false;
			}
		} catch(IOException e) {
			System.err.println("[sTablist-AutoUpdater] Website seems to be down... Skipping update.");
		}
		
		return true;
	}
	
	private boolean downloadUpdate(final CommandSender sender) {
		boolean success = false;
		String pluginPath = "plugins/" + Tablist.getPlugin(Tablist.class).getDescription().getName() + ".jar";
		
		try {
			sender.sendMessage(ChatColor.GREEN + "Downloading...");
			
			int count = downloadFile("http://mc.jumpy91.de/Jul1anUpdater/sTablist.jar", pluginPath);
			
			sender.sendMessage(ChatColor.GREEN + "Downloading...");
			
			sender.sendMessage(ChatColor.GREEN + "Plugin downloaded! (" + count / 1024 + "KB)");
			sender.sendMessage(ChatColor.GREEN + "Reloading plugin...");
			
			reload();
			
			sender.sendMessage(ChatColor.GREEN + "Update successfully.");
			success = true;
			
		} catch(Exception e) {
			sender.sendMessage(ChatColor.RED + "Failed to update: " + e.getMessage());
		}
		
		return success;
	}
	
	@SuppressWarnings("unchecked")
	private void reload() throws UnknownDependencyException, InvalidPluginException, InvalidDescriptionException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		PluginManager manager = Tablist.getPlugin(Tablist.class).getServer().getPluginManager();
		SimplePluginManager spmanager = (SimplePluginManager) manager;
		// unload
		Plugin pluginunload = manager.getPlugin(Tablist.getPlugin(Tablist.class).getDescription().getName());
		manager.disablePlugin(pluginunload);
		
		if(spmanager != null) {
			Field pluginsField = spmanager.getClass().getDeclaredField("plugins");
			pluginsField.setAccessible(true);
			List<Plugin> plugins = (List<Plugin>) pluginsField.get(spmanager);
			
			Field lookupNamesField = spmanager.getClass().getDeclaredField("lookupNames");
			lookupNamesField.setAccessible(true);
			Map<String, Plugin> lookupNames = (Map<String, Plugin>) lookupNamesField.get(spmanager);
			
			Field commandMapField = spmanager.getClass().getDeclaredField("commandMap");
			commandMapField.setAccessible(true);
			SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(spmanager);
			
			Field knownCommandsField = null;
			Map<String, Command> knownCommands = null;
			
			if(commandMap != null) {
				knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
				knownCommandsField.setAccessible(true);
				knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
			}
			
			for(Plugin plugin : manager.getPlugins()) {
				if(plugin.getDescription().getName().equalsIgnoreCase(Tablist.getPlugin(Tablist.class).getDescription().getName())) {
					manager.disablePlugin(plugin);
					
					if(plugins != null && plugins.contains(plugin)) {
						plugins.remove(plugin);
					}
					
					if(lookupNames != null && lookupNames.containsKey(Tablist.getPlugin(Tablist.class).getDescription().getName())) {
						lookupNames.remove(Tablist.getPlugin(Tablist.class).getDescription().getName());
					}
					
					if(commandMap != null) {
						for(Iterator<Map.Entry<String, Command>> it = knownCommands.entrySet().iterator(); it.hasNext();) {
							Map.Entry<String, Command> entry = it.next();
							
							if(entry.getValue() instanceof PluginCommand) {
								PluginCommand command = (PluginCommand) entry.getValue();
								
								if(command.getPlugin() == plugin) {
									command.unregister(commandMap);
									it.remove();
								}
							}
						}
					}
				}
			}
		}
		
		// load
		Plugin pluginload = manager.loadPlugin(new File("plugins", Tablist.getPlugin(Tablist.class).getDescription().getName() + ".jar"));
		pluginload.onLoad();
		manager.enablePlugin(pluginload);
	}
	
	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
		if(!cs.hasPermission("sTablist.Update")) {
			return true;
		}
		downloadUpdate(cs);
		return true;
	}
	
}
