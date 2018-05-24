package me.jul1an_k.tablist.bungee.update;

import static me.jul1an_k.tablist.api.HTTPApi.downloadFile;
import static me.jul1an_k.tablist.api.HTTPApi.readLine;

import java.io.IOException;

import me.jul1an_k.tablist.bungee.Tablist;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class FileUpdate {
	
	public void check() {
		try {
			String newVersion = readLine("http://mc.jumpy91.de/Jul1anUpdater/sTablistUpdate.txt");
			String oldVersion = Tablist.getInstance().getDescription().getVersion();
			System.out.println("[sTablist-AutoUpdater] Newest Version: " + newVersion + "! Current Version: " + oldVersion);
			if(!newVersion.equals(oldVersion)) {
				String changesString = readLine("http://mc.jumpy91.de/Jul1anUpdater/sTablistChanges.txt");
				System.out.println("[sTablist-AutoUpdater] New in this version: " + changesString);
				for(ProxiedPlayer all : ProxyServer.getInstance().getPlayers()) {
					if(all.hasPermission("sTablist.Update")) {
						all.sendMessage(new TextComponent("§aAn Update for sTablist is available!"));
						all.sendMessage(new TextComponent("§cCurrent Version: " + oldVersion));
						all.sendMessage(new TextComponent("§2New Version: " + newVersion));
						all.sendMessage(new TextComponent("§3New in this version: " + changesString));
						all.sendMessage(new TextComponent("§aDownloading update..."));
					}
				}
				updateDownload();
				for(ProxiedPlayer all : ProxyServer.getInstance().getPlayers()) {
					if(all.hasPermission("sTablist.Update")) {
						all.sendMessage(new TextComponent("§aDownloaded update!"));
					}
				}
			}
		} catch(IOException e) {
			System.err.println("[sTablist-AutoUpdater] Website seems to be down... Skipping update.");
		}
	}
	
	public boolean updateDownload() {
		boolean success = false;
		String pluginPath = "plugins/" + Tablist.getInstance().getDescription().getName() + ".jar";
		
		try {
			System.out.println(ChatColor.GREEN + "Downloading...");
			
			int count = downloadFile("http://mc.jumpy91.de/Jul1anUpdater/sTablist.jar", pluginPath);
			
			System.out.println(ChatColor.GREEN + "Downloading...");
			
			System.out.println(ChatColor.GREEN + "Plugin downloaded! (" + count / 1024 + "KB)");
			
			System.out.println(ChatColor.GREEN + "Update successfully.");
			success = true;
			
		} catch(Exception e) {
			System.out.println(ChatColor.RED + "Failed to update: " + e.getMessage());
		}
		
		return success;
	}
	
}
