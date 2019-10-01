package me.jul1an_k.tablist.bukkit.api.impl.tablistapi;

import me.jul1an_k.tablist.api.bukkit.sTablistAPI;
import me.jul1an_k.tablist.bukkit.variables.VariableManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TablistAPI_1_14 extends sTablistAPI {

	private final Class<?> craftPlayer;
	private final Class<?> entityPlayer;

	private Method craftPlayer$getHandle;

	public TablistAPI_1_14() {
		this.craftPlayer = getOBCClass("entity.CraftPlayer");
		this.entityPlayer = getNMSClass("EntityPlayer");

		try {
			this.craftPlayer$getHandle = craftPlayer.getMethod("getHandle");
		} catch(NoSuchMethodException ignored) {}
	}
	
	public void sendTabList(Player player, String header, String footer) {
		Validate.notNull(player, "Player cannot be null");

		if(footer == null) {
			player.setPlayerListHeader(VariableManager.replaceTab(header, player));
		} else if(header == null) {
			player.setPlayerListFooter(VariableManager.replaceTab(footer, player));
		} else {
			player.setPlayerListHeaderFooter(VariableManager.replaceTab(header, player), VariableManager.replaceTab(footer, player));
		}
	}
	
	public void sendActionBar(Player player, String message) {
		Validate.notNull(player, "Player cannot be null");
		Validate.notNull(message, "Message cannot be null");

		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(VariableManager.replace(message, player)));
	}
	
	public void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut, boolean clear, boolean reset) {
		Validate.notNull(player, "Player cannot be null");
		Validate.notNull(title, "Title cannot be null");
		Validate.notNull(subTitle, "Subtitle cannot be null");

		player.sendTitle(VariableManager.replace(title, player), VariableManager.replace(subTitle, player), fadeIn, stay, fadeOut);

		if(clear) {

		}

		if(reset) {
			player.resetTitle();
		}
	}
	
	public String getVersion() {
		String name = Bukkit.getServer().getClass().getPackage().getName();
		return name.substring(name.lastIndexOf('.') + 1) + ".";
	}
	
	private Class<?> getNMSClass(String className) {
		String fullName = "net.minecraft.server." + getVersion() + className;
		Class<?> clazz = null;
		try {
			clazz = Class.forName(fullName);
		} catch(ClassNotFoundException e) {
			System.err.println("[sTablistAPI] Can't find the Class '" + fullName + "'!");
		} catch(Exception e) {
			e.printStackTrace();
		}
		return clazz;
	}
	
	private Class<?> getOBCClass(String className) {
		String fullName = "org.bukkit.craftbukkit." + getVersion() + className;
		Class<?> clazz = null;
		try {
			clazz = Class.forName(fullName);
		} catch(ClassNotFoundException e) {
			System.err.println("[sTablistAPI] Can't find the Class '" + fullName + "'!");
		}
		return clazz;
	}
	
	private Object getNMSPlayer(Player p) {
		try {
			return craftPlayer$getHandle.invoke(p);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Field getField(Field f) {
		f.setAccessible(true);
		return f;
	}

	public int getPing(Player player) {
		int pingInt = 0;

		Object nmsPlayer = getNMSPlayer(player);

		try {
			Field ping = getField(entityPlayer.getField("ping"));

			pingInt = ping.getInt(nmsPlayer);
		} catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return pingInt;
	}
	
}
