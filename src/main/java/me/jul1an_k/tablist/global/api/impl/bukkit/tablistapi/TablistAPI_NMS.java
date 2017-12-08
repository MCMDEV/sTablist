package me.jul1an_k.tablist.global.api.impl.bukkit.tablistapi;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.jul1an_k.tablist.bukkit.sTablistAPI;
import me.jul1an_k.tablist.bukkit.variables.VariableManager;

public class TablistAPI_NMS extends sTablistAPI {
	
	private Class<?> chatserial;
	private Class<?> title;
	private Class<?> enumtitleaction;
	
	public TablistAPI_NMS() {
		if(compareMinecraftVersionServerIsHigherOrEqual("1.8.3")) {
			chatserial = getNMSClass("IChatBaseComponent$ChatSerializer");
			title = getNMSClass("PacketPlayOutTitle");
			enumtitleaction = getNMSClass("PacketPlayOutTitle$EnumTitleAction");
		} else if(compareMinecraftVersionServerIsHigherOrEqual("1.8")) {
			chatserial = getNMSClass("ChatSerializer");
			title = getNMSClass("PacketPlayOutTitle");
			enumtitleaction = getNMSClass("EnumTitleAction");
		}
	}
	
	public void sendTabList(Player player, String header, String footer) {
		try {
			if(header != null)
				header = VariableManager.replaceTab(header, player);
			
			if(footer != null)
				footer = VariableManager.replaceTab(footer, player);
			
			Object packet = getNMSClass("PacketPlayOutPlayerListHeaderFooter").newInstance();
			if(header != null) {
				getField(packet.getClass().getDeclaredField("a")).set(packet, chatserial.getMethod("a", String.class).invoke(null, "{\"text\": \"" + header + "\"}"));
			}
			if(footer != null) {
				getField(packet.getClass().getDeclaredField("b")).set(packet, chatserial.getMethod("a", String.class).invoke(null, "{\"text\": \"" + footer + "\"}"));
			}
			
			sendPacket(player, packet);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendActionBar(Player player, String message) {
		if(message != null)
			message = VariableManager.replace(message, player);
		
		try {
			if(compareMinecraftVersionServerIsHigherOrEqual("1.12")) {
				sendPacket(player, "PacketPlayOutChat", new Class[] { getNMSClass("IChatBaseComponent"), getNMSClass("ChatMessageType") }, chatserial.getMethod("a", String.class).invoke(null, "{\"text\": \"" + message + "\"}"), getNMSClass("ChatMessageType").getMethod("a", byte.class).invoke(null, (byte) 2));
			} else if(compareMinecraftVersionServerIsHigherOrEqual("1.8")) {
				sendPacket(player, "PacketPlayOutChat", new Class[] { getNMSClass("IChatBaseComponent"), byte.class }, chatserial.getMethod("a", String.class).invoke(null, "{\"text\": \"" + message + "\"}"), (byte) 2);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void sendTitle(Player p, String title, String subtitle, int fadein, int stay, int fadeout, boolean clear, boolean reset) {
		title = VariableManager.replace(title, p);
		subtitle = VariableManager.replace(subtitle, p);
		
		try {
			Object t = this.title.newInstance();
			Field f = t.getClass().getDeclaredField("a");
			f.setAccessible(true);
			f.set(t, getField(enumtitleaction.getDeclaredField("TITLE")).get(null));
			f = t.getClass().getDeclaredField("b");
			f.setAccessible(true);
			f.set(t, chatserial.getMethod("a", String.class).invoke(null, "{\"text\": \"" + title + "\"}"));
			sendPacket(p, t);
			
			t = this.title.newInstance();
			f = t.getClass().getDeclaredField("a");
			f.setAccessible(true);
			f.set(t, getField(enumtitleaction.getDeclaredField("SUBTITLE")).get(null));
			f = t.getClass().getDeclaredField("b");
			f.setAccessible(true);
			f.set(t, chatserial.getMethod("a", String.class).invoke(null, "{\"text\": \"" + subtitle + "\"}"));
			sendPacket(p, t);
			
			t = this.title.newInstance();
			f = t.getClass().getDeclaredField("a");
			f.setAccessible(true);
			f.set(t, getField(enumtitleaction.getDeclaredField("TIMES")).get(null));
			f = t.getClass().getDeclaredField("c");
			f.setAccessible(true);
			f.set(t, fadein);
			f = t.getClass().getDeclaredField("d");
			f.setAccessible(true);
			f.set(t, stay);
			f = t.getClass().getDeclaredField("e");
			f.setAccessible(true);
			f.set(t, fadeout);
			sendPacket(p, t);
			
			if(clear) {
				t = this.title.newInstance();
				f = t.getClass().getDeclaredField("a");
				f.setAccessible(true);
				f.set(t, getField(enumtitleaction.getDeclaredField("CLEAR")).get(null));
				sendPacket(p, t);
			}
			
			if(reset) {
				t = this.title.newInstance();
				f = t.getClass().getDeclaredField("a");
				f.setAccessible(true);
				f.set(t, getField(enumtitleaction.getDeclaredField("RESET")).get(null));
				sendPacket(p, t);
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
