package me.jul1an_k.tablist.global.api.impl.bukkit.tablistapi;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.jul1an_k.tablist.bukkit.sTablistAPI;
import me.jul1an_k.tablist.bukkit.variables.VariableManager;

public class TablistAPI_ProtocolHack extends sTablistAPI {
	
	private Class<?> chatserial;
	private Class<?> title;
	private Class<?> enumtitleaction;
	
	public TablistAPI_ProtocolHack() {
		chatserial = getNMSClass("ChatSerializer");
		title = getProtocolInjectorClass("PacketTitle");
		enumtitleaction = getProtocolInjectorClass("PacketTitle$Action");
	}
	
	public void sendTabList(Player player, String header, String footer) {
		try {
			if(header != null)
				header = VariableManager.replaceTab(header, player);
			
			if(footer != null)
				footer = VariableManager.replaceTab(footer, player);
			
			Object packet = getProtocolInjectorClass("PacketTabHeader").newInstance();
			
			if(header != null)
				getField(packet.getClass().getDeclaredField("header")).set(packet, chatserial.getMethod("a", String.class).invoke(null, "{\"text\": \"" + header + "\"}"));
			
			if(footer != null)
				getField(packet.getClass().getDeclaredField("footer")).set(packet, chatserial.getMethod("a", String.class).invoke(null, "{\"text\": \"" + footer + "\"}"));
			
			sendPacket(player, packet);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendActionBar(Player player, String message) {
		if(message != null)
			message = VariableManager.replace(message, player);
		
		try {
			sendPacket(player, "PacketPlayOutChat", new Class[] { getNMSClass("IChatBaseComponent"), byte.class }, chatserial.getMethod("a", String.class).invoke(null, "{\"text\": \"" + message + "\"}"), (byte) 2);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendTitle(Player player, String title, String subtitle, int fadein, int stay, int fadeout, boolean clear, boolean reset) {
		title = VariableManager.replace(title, player);
		subtitle = VariableManager.replace(subtitle, player);
		
		try {
			Object t = this.title.newInstance();
			Field f = t.getClass().getDeclaredField("action");
			f.setAccessible(true);
			f.set(t, getField(enumtitleaction.getDeclaredField("TITLE")).get(null));
			f = t.getClass().getDeclaredField("text");
			f.setAccessible(true);
			f.set(t, chatserial.getMethod("a", String.class).invoke(null, "{\"text\": \"" + title + "\"}"));
			sendPacket(player, t);
			
			t = this.title.newInstance();
			f = t.getClass().getDeclaredField("action");
			f.setAccessible(true);
			f.set(t, getField(enumtitleaction.getDeclaredField("SUBTITLE")).get(null));
			f = t.getClass().getDeclaredField("text");
			f.setAccessible(true);
			f.set(t, chatserial.getMethod("a", String.class).invoke(null, "{\"text\": \"" + subtitle + "\"}"));
			sendPacket(player, t);
			
			t = this.title.newInstance();
			f = t.getClass().getDeclaredField("action");
			f.setAccessible(true);
			f.set(t, getField(enumtitleaction.getDeclaredField("TIMES")).get(null));
			f = t.getClass().getDeclaredField("fadeIn");
			f.setAccessible(true);
			f.set(t, fadein);
			f = t.getClass().getDeclaredField("stay");
			f.setAccessible(true);
			f.set(t, stay);
			f = t.getClass().getDeclaredField("fadeOut");
			f.setAccessible(true);
			f.set(t, fadeout);
			sendPacket(player, t);
			
			if(clear) {
				t = this.title.newInstance();
				f = t.getClass().getDeclaredField("action");
				f.setAccessible(true);
				f.set(t, getField(enumtitleaction.getDeclaredField("CLEAR")).get(null));
				sendPacket(player, t);
			}
			
			if(reset) {
				t = this.title.newInstance();
				f = t.getClass().getDeclaredField("action");
				f.setAccessible(true);
				f.set(t, getField(enumtitleaction.getDeclaredField("RESET")).get(null));
				sendPacket(player, t);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendPacket(Player p, Object packet) {
		try {
			Object nmsPlayer = getNMSPlayer(p);
			Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
			connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, packet);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendPacket(Player p, String packetName, Class<?>[] parameterclass, Object... parameters) {
		try {
			Object nmsPlayer = getNMSPlayer(p);
			Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
			Object packet = Class.forName(nmsPlayer.getClass().getPackage().getName() + "." + packetName).getConstructor(parameterclass).newInstance(parameters);
			connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, packet);
		} catch(Exception e) {
			e.printStackTrace();
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
	
	private Class<?> getProtocolInjectorClass(String className) {
		String fullName = "org.spigotmc.ProtocolInjector$" + className;
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
			return p.getClass().getMethod("getHandle").invoke(p);
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
			Field ping = nmsPlayer.getClass().getField("ping");

			pingInt = ping.getInt(nmsPlayer);
		} catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return pingInt;
	}
	
}
