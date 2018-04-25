package me.jul1an_k.tablist.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.jul1an_k.tablist.global.api.impl.bukkit.tablistapi.TablistAPI_Glowstone;
import me.jul1an_k.tablist.global.api.impl.bukkit.tablistapi.TablistAPI_NMS;
import me.jul1an_k.tablist.global.api.impl.bukkit.tablistapi.TablistAPI_ProtocolHack;

public abstract class sTablistAPI {
	
	private static sTablistAPI implemantation;
	
	public abstract void sendTabList(Player player, String header, String footer);
	
	public abstract void sendActionBar(Player player, String message);
	
	public abstract void sendTitle(Player player, String title, String subtitle, int fadein, int stay, int fadeout, boolean clear, boolean reset);
	
	public abstract int getPing(Player player);
	
	public abstract String getVersion();
	
	public static sTablistAPI getImpl() {
		return implemantation;
	}
	
	static void setupTablistAPI() {
		try {
			Class.forName("net.glowstone.entity.GlowPlayer");
			
			implemantation = new TablistAPI_Glowstone();
			
			System.out.println("[sTablistAPI] TablistAPI Implementation set to Glowstone.");
			
			return;
		} catch(ClassNotFoundException e) {}
		
		try {
			Class.forName("org.spigotmc.ProtocolInjector$PacketTabHeader");
			
			implemantation = new TablistAPI_ProtocolHack();
			
			System.out.println("[sTablistAPI] TablistAPI Implementation set to 1.7.10 'Protocol Hack'.");
			
			return;
		} catch(ClassNotFoundException e) {}
		
		implemantation = new TablistAPI_NMS();
		
		System.out.println("[sTablistAPI] TablistAPI Implementation set to NMS. (1.8.X - 1.12.X)");
	}
	
	public boolean compareMinecraftVersionServerIsHigherOrEqual(String version) {
		String serverVersion = Bukkit.getVersion();
		serverVersion = serverVersion.substring(serverVersion.indexOf("(MC: ") + 5, serverVersion.length());
		serverVersion = serverVersion.substring(0, serverVersion.lastIndexOf(")"));
		String[] serverVersionArray = serverVersion.split("\\.");
		String[] toCompareVersionArray = version.split("\\.");
		
		if(serverVersionArray.length == 2) {
			int serverFirst = Integer.valueOf(serverVersionArray[0]);
			int toCompareFirst = Integer.valueOf(toCompareVersionArray[0]);
			if(toCompareFirst != serverFirst) {
				return toCompareFirst < serverFirst;
			}
			int serverSecond = Integer.valueOf(serverVersionArray[1]);
			int toCompareSecond = Integer.valueOf(toCompareVersionArray[1]);
			if(toCompareSecond != serverSecond) {
				return toCompareSecond < serverSecond;
			}
			if(toCompareVersionArray.length == 3) {
				return false;
			}
			return true;
		}
		if(serverVersionArray.length == 3) {
			int serverFirst = Integer.valueOf(serverVersionArray[0]);
			int toCompareFirst = Integer.valueOf(toCompareVersionArray[0]);
			if(toCompareFirst != serverFirst) {
				return toCompareFirst < serverFirst;
			}
			int serverSecond = Integer.valueOf(serverVersionArray[1]);
			int toCompareSecond = Integer.valueOf(toCompareVersionArray[1]);
			if(toCompareSecond != serverSecond) {
				return toCompareSecond < serverSecond;
			}
			if(toCompareVersionArray.length != 3) {
				return true;
			}
			int serverThird = Integer.valueOf(serverVersionArray[2]);
			int toCompareThird = Integer.valueOf(toCompareVersionArray[2]);
			if(toCompareThird != serverThird) {
				return toCompareThird < serverThird;
			}
			return true;
		}
		return false;
	}
	
}
