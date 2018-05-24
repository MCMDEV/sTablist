package me.jul1an_k.tablist.bukkit.commands;

import me.jul1an_k.tablist.api.bukkit.sTablistAPI;
import me.jul1an_k.tablist.bukkit.Tablist;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;

public class InfoCommand implements CommandExecutor {
	
	private String economyName = "Not found | Can't find Vault";
	private String permissionsName = "Not found | Can't find Vault";
	
	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
		cs.sendMessage("§b[]======[] §6sTablist §7- §6INFO §b[]======[]");
		cs.sendMessage("§8Version: §e" + Tablist.getPlugin(Tablist.class).getDescription().getVersion());
		cs.sendMessage("§8Packets: §e" + sTablistAPI.getImpl().getVersion().replace(".", "").replace("v", ""));
		if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			net.milkbowl.vault.economy.Economy economy = null;
			RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = Tablist.getPlugin(Tablist.class).getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if(economyProvider != null) {
				economy = economyProvider.getProvider();
			}
			
			net.milkbowl.vault.permission.Permission permission = null;
			RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = Tablist.getPlugin(Tablist.class).getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
			if(permissionProvider != null) {
				permission = permissionProvider.getProvider();
			}
			
			if(economy != null) {
				economyName = economy.getName();
			} else {
				economyName = "Not found | Can't find an Economy Plugin";
			}
			
			if(permission != null) {
				permissionsName = permission.getName();
			} else {
				permissionsName = "Not found | Can't find an Permission Plugin";
			}
		}
		cs.sendMessage("§8Economy: §e" + economyName);
		cs.sendMessage("§8Permissions: §e" + permissionsName);
		if(Bukkit.getPluginManager().isPluginEnabled("Vault") && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			cs.sendMessage("§8Variable Addons: §ePvP Variables §8(§e1.1.6§8)§7, §eVault Variables §8(§e1.0.1§8)§7, §ePlaceholderAPI Variables §8(§e2.0.3§8)");
		} else if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			cs.sendMessage("§8Variable Addons: §ePvP Variables §8(§e1.1.6§8)§7, §eVault Variables §8(§e1.0.1§8)");
		} else if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			cs.sendMessage("§8Variable Addons: §ePvP Variables §8(§e1.1.6§8)§7, §ePlaceholderAPI Variables §8(§e2.0.3§8)");
		} else {
			cs.sendMessage("§8Variable Addons: §ePvP Variables §8(§e1.1.6§8)");
		}
		cs.sendMessage("§b[]======[] §6sTablist §7- §6INFO §b[]======[]");
		return true;
	}
	
}
