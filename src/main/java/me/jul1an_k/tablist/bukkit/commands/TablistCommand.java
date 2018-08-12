package me.jul1an_k.tablist.bukkit.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.jul1an_k.tablist.api.bukkit.sTablistAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.jul1an_k.tablist.bukkit.Tablist;
import me.jul1an_k.tablist.bukkit.tabprefix.TabPrefix;
import org.bukkit.plugin.java.JavaPlugin;

public class TablistCommand implements CommandExecutor, TabCompleter {

	private final Tablist plugin;

	public TablistCommand() {
		plugin = JavaPlugin.getPlugin(Tablist.class);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
		if(!cs.hasPermission("sTablist.help")) {
			cs.sendMessage("§4You don't have permission to use this command!");
			return true;
		}

		if(args.length == 1) {
			if(args[0].equalsIgnoreCase("regenConfig")) {
				if(!cs.hasPermission("sTablist.regenConfig")) {
					cs.sendMessage("§4You don't have permission to use this command!");
					return true;
				}
				
				File f = new File("plugins/sTablist", "config.yml");
				
				if(!f.exists()) {
					cs.sendMessage("§cThe config doesn't exists.");
					return true;
				}
				
				f.renameTo(new File("plugins/sTablist", "config.yml.backup"));
				// f.delete();
				
				plugin.saveDefaultConfig();
				
				cs.sendMessage("§aConfig regenerated.");
			} else if(args[0].equalsIgnoreCase("reload")) {
				if(!cs.hasPermission("sTablist.reload")) {
					cs.sendMessage("§4You don't have permission to use this command!");
					return true;
				}

				plugin.reloadConfig();

				Tablist.stopUpdate();
				Tablist.startUpdate();

				for(String group : TabPrefix.getGroupsFile().getYaml().getConfigurationSection("").getKeys(false)) {
					if(group.equalsIgnoreCase("GroupSort")) {
						continue;
					}

					TabPrefix.getImpl().setupGroup(group, TabPrefix.getGroupsFile().getYaml().getString(group + ".Prefix"), TabPrefix.getGroupsFile().getYaml().getString(group + ".Suffix"));
				}

				cs.sendMessage("§aSuccessfully reloaded the Configuration File!");
			} else if(args[0].equalsIgnoreCase("copyFromPermissionSystem")) {
				if(!cs.hasPermission("sTablist.copyFromPermissionSystem")) {
					cs.sendMessage("§4You don't have permission to use this command!");
					return true;
				}
				
				if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
					net.milkbowl.vault.chat.Chat chat = null;
					RegisteredServiceProvider<net.milkbowl.vault.chat.Chat> chatProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
					if(chatProvider != null) {
						chat = chatProvider.getProvider();
					}
					
					net.milkbowl.vault.permission.Permission permission = null;
					RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
					if(permissionProvider != null) {
						permission = permissionProvider.getProvider();
					}
					
					if(permission == null) {
						cs.sendMessage("§cCan't find a PermissionSystem");
						return true;
					}
					
					if(chat == null) {
						cs.sendMessage("§cCan't find a ChatSystem");
						return true;
					}
					
					File file = new File("plugins/sTablist/Prefixes-And-Suffixes", "groups.yml");
					
					if(file.exists()) {
						file.delete();
					}
					
					try {
						file.createNewFile();
					} catch(IOException e) {
						e.printStackTrace();
					}
					
					FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
					
					for(String s : permission.getGroups()) {
						cfg.set(s + ".Prefix", chat.getGroupPrefix(Bukkit.getWorlds().get(0), s));
						cfg.set(s + ".Suffix", chat.getGroupSuffix(Bukkit.getWorlds().get(0), s));
					}
					
					cfg.set("GroupSort", Collections.singletonList(permission.getGroups()[0]));
					
					cs.sendMessage("§aSuccessfully imported all Groups from " + permission.getName());
				} else {
					cs.sendMessage("§cCan't find Vault!");
				}
			} else if(args[0].equalsIgnoreCase("info")) {
				String economyName = "Not found | Can't find Vault";
				String permissionsName = "Not found | Can't find Vault";

				cs.sendMessage("§b[]======[] §6sTablist §7- §6INFO §b[]======[]");
				cs.sendMessage("§8Version: §e" + Tablist.getPlugin(Tablist.class).getDescription().getVersion());
				cs.sendMessage("§8API-Implementation: §e" + sTablistAPI.getImpl().getVersion().replace(".", "").replace("v", ""));
				if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
					net.milkbowl.vault.economy.Economy economy = null;
					RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
					if(economyProvider != null) {
						economy = economyProvider.getProvider();
					}

					net.milkbowl.vault.permission.Permission permission = null;
					RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
					if(permissionProvider != null) {
						permission = permissionProvider.getProvider();
					}

					economyName = economy != null ? economy.getName() : "Not found | Can't find an Economy Plugin";
					permissionsName = permission != null ? permission.getName() : "Not found | Can't find a Permission Plugin";
				}

				cs.sendMessage("§8Economy: §e" + economyName);
				cs.sendMessage("§8Permissions: §e" + permissionsName);
				if(Bukkit.getPluginManager().isPluginEnabled("Vault") && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
					cs.sendMessage("§8Variable Addons: §ePvP Variables§7, §eVault Variables§7, §ePlaceholderAPI Variables");
				} else if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
					cs.sendMessage("§8Variable Addons: §ePvP Variables§7, §eVault Variables");
				} else if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
					cs.sendMessage("§8Variable Addons: §ePvP Variables§7, §ePlaceholderAPI Variables");
				} else {
					cs.sendMessage("§8Variable Addons: §ePvP Variables");
				}
				cs.sendMessage("§b[]======[] §6sTablist §7- §6INFO §b[]======[]");
				return true;
			}
		} else if(args.length == 2) {
			if(args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("delsuffix") || args[0].equalsIgnoreCase("delprefix")) {
				if(!cs.hasPermission("sTablist.del")) {
					cs.sendMessage("§4You don't have permission to use this command!");
					return true;
				}
				
				OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
				
				TabPrefix.getImpl().unset(p);
				cs.sendMessage("§aThe Prefix and Suffix of §6" + p.getName() + " §awas deleted.");
			}
		} else if(args.length >= 3) {
			 if(args[0].equalsIgnoreCase("setHeader")) {
				if(!cs.hasPermission("sTablist.setTab")) {
					cs.sendMessage("§4You don't have permission to use this command!");
					return true;
				}

				String header = ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
				plugin.getConfig().set("Header.text", header);
				plugin.saveConfig();
				plugin.reloadConfig();

				cs.sendMessage("§aSuccessfully changed header to §6" + header + "§a.");
			} else if(args[0].equalsIgnoreCase("setFooter")) {
				if(!cs.hasPermission("sTablist.setTab")) {
					cs.sendMessage("§4You don't have permission to use this command!");
					return true;
				}

				String footer = ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
				plugin.getConfig().set("Footer.text", footer);
				plugin.saveConfig();
				plugin.reloadConfig();

				cs.sendMessage("§aSuccessfully changed footer to §6" + footer + "§a.");
			} else if(args[0].equalsIgnoreCase("bc") | args[0].equalsIgnoreCase("broadcast")) {
				if(!cs.hasPermission("sTablist.broadcast")) {
					cs.sendMessage("§4You don't have permission to use this command!");
					return true;
				}

				String msg = ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.copyOfRange(args, 2, args.length)));

				if(args[1].equalsIgnoreCase("Title")) {
					for(Player all : Bukkit.getOnlinePlayers()) {
						sTablistAPI.getImpl().sendTitle(all, msg, "", 5, 20, 5, false, false);
					}
				} else if(args[1].equalsIgnoreCase("Subtitle")) {
					for(Player all : Bukkit.getOnlinePlayers()) {
						sTablistAPI.getImpl().sendTitle(all, "", msg, 5, 20, 5, false, false);
					}
				} else if(args[1].equalsIgnoreCase("ActionBar")) {
					for(Player all : Bukkit.getOnlinePlayers()) {
						sTablistAPI.getImpl().sendActionBar(all, msg);
					}
				}
			} else if(args[0].equalsIgnoreCase("setPrefix")) {
				if(!cs.hasPermission("sTablist.setPrefix")) {
					cs.sendMessage("§4You don't have permission to use this command!");
					return true;
				}
				Player p = Bukkit.getPlayer(args[1]);

				String msg = ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.copyOfRange(args, 2, args.length)));

				if(msg.length() > 16) {
					cs.sendMessage("§4The Prefix has a maximal length of 16!");
					return true;
				}
				TabPrefix.getImpl().setPrefix(p, msg);
				cs.sendMessage("§aThe Prefix of §6" + p.getName() + " §awas set to §6" + msg);
			} else if(args[0].equalsIgnoreCase("setSuffix")) {
				if(!cs.hasPermission("sTablist.setSuffix")) {
					cs.sendMessage("§4You don't have permission to use this command!");
					return true;
				}
				Player p = Bukkit.getPlayer(args[1]);

				String msg = ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.copyOfRange(args, 2, args.length)));

				if(msg.length() > 16) {
					cs.sendMessage("§4The Suffix has a maximal length of 16!");
					return true;
				}

				TabPrefix.getImpl().setSuffix(p, msg);
				cs.sendMessage("§aThe Suffix of §6" + p.getName() + " §awas set to §6" + msg);
			}
			if(args[0].equalsIgnoreCase("msg") | args[0].equalsIgnoreCase("message") | args[0].equalsIgnoreCase("pmsg")) {
				if(!cs.hasPermission("sTablist.msg")) {
					cs.sendMessage("§4You don't have permission to use this command!");
					return true;
				}
				Player p = Bukkit.getPlayer(args[1]);
				if(args[2].equalsIgnoreCase("Title")) {
					String msg = ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.copyOfRange(args, 3, args.length)));

					sTablistAPI.getImpl().sendTitle(p, msg, "", 5, 20, 5, false, false);
				} else if(args[2].equalsIgnoreCase("Subtitle")) {
					String msg = ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.copyOfRange(args, 3, args.length)));

					sTablistAPI.getImpl().sendTitle(p, "", msg, 5, 20, 5, false, false);
				} else if(args[2].equalsIgnoreCase("ActionBar")) {
					String msg = ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.copyOfRange(args, 3, args.length)));

					sTablistAPI.getImpl().sendActionBar(p, msg);
				}
			}
		} else {
			cs.sendMessage("§b[]======[] §6sTablist §7- §6HELP §b[]======[]");
			cs.sendMessage("§e/sTablist bc|broadcast <Type> <Message>");
			cs.sendMessage("§e/sTablist msg|message|pmsg <Player> <Type> <Message>");
			cs.sendMessage("§e/sTablist setPrefix <Player> <Prefix>");
			cs.sendMessage("§e/sTablist setSuffix <Player> <Suffix>");
			cs.sendMessage("§e/sTablist setHeader <Header>");
			cs.sendMessage("§e/sTablist setFooter <Footer>");
			cs.sendMessage("§e/sTablist del <Player>");
			cs.sendMessage("§e/sTablist regenConfig");
			cs.sendMessage("§e/sTablist copyFromPermissionSystem");
			cs.sendMessage("§e/sTablist reload");
			cs.sendMessage("§e/sTablist info");
			cs.sendMessage("§eAvailable Types: §7Title§8, §7Subtitle§8, §7ActionBar");
			cs.sendMessage("§b[]======[] §6sTablist §7- §6HELP §b[]======[]");
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender cs, Command command, String label, String[] args) {
		if(args.length == 0) {
			return Arrays.asList("broadcast", "message", "setprefix", "setsuffix", "setHeader", "setFooter", "del", "regenconfig", "copyfrompermissionsystem", "reload", "info");
		} else if(args.length == 1) {
			if(args[0].equalsIgnoreCase("bc") || args[0].equalsIgnoreCase("broadcast")) {
				return Arrays.asList("title", "subtitle", "actionbar");
			}
		} else if(args.length == 2) {
			if(args[0].equalsIgnoreCase("msg") || args[0].equalsIgnoreCase("message") || args[0].equalsIgnoreCase("pmsg")) {
				return Arrays.asList("title", "subtitle", "actionbar");
			}
		}

		return null;
	}

}
