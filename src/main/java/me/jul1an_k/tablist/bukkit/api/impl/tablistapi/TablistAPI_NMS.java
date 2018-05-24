package me.jul1an_k.tablist.bukkit.api.impl.tablistapi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.jul1an_k.tablist.api.bukkit.sTablistAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.jul1an_k.tablist.bukkit.variables.VariableManager;

public class TablistAPI_NMS extends sTablistAPI {
	
	private Class<?> chatSerializer;
	private Class<?> packetPlayOutTitle;
	private Class<?> enumTitleAction;
	private Class<?> chatMessageType;
	private Class<?> packet;
	private Class<?> craftPlayer;
	private Class<?> entityPlayer;
	private Class<?> playerConnection;

	private Method chatSerializer$a;
	private Method craftPlayer$getHandle;
	private Method playerConnection$sendPacket;
	
	public TablistAPI_NMS() {
		if(compareMinecraftVersionServerIsHigherOrEqual("1.8.3")) {
			this.chatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
			this.packetPlayOutTitle = getNMSClass("PacketPlayOutTitle");
			this.enumTitleAction = getNMSClass("PacketPlayOutTitle$EnumTitleAction");
		} else if(compareMinecraftVersionServerIsHigherOrEqual("1.8")) {
			this.chatSerializer = getNMSClass("ChatSerializer");
			this.packetPlayOutTitle = getNMSClass("PacketPlayOutTitle");
			this.enumTitleAction = getNMSClass("EnumTitleAction");
		}

		this.chatMessageType = getNMSClass("ChatMessageType");
		this.packet = getNMSClass("Packet");
		this.craftPlayer = getOBCClass("entity.CraftPlayer");
		this.entityPlayer = getNMSClass("EntityPlayer");
		this.playerConnection = getNMSClass("PlayerConnection");

		try {
			this.chatSerializer$a = chatSerializer.getMethod("a", String.class);
			this.craftPlayer$getHandle = craftPlayer.getMethod("getHandle");
			this.playerConnection$sendPacket = playerConnection.getMethod("sendPacket", packet);
		} catch(NoSuchMethodException exception) {}
	}
	
	public void sendTabList(Player player, String header, String footer) {
		try {
			if(header != null)
				header = VariableManager.replaceTab(header, player);
			
			if(footer != null)
				footer = VariableManager.replaceTab(footer, player);
			
			Object packet = getNMSClass("PacketPlayOutPlayerListHeaderFooter").newInstance();
			if(header != null) {
				getField(packet.getClass().getDeclaredField("a")).set(packet, this.chatSerializer$a.invoke(null, "{\"text\": \"" + header + "\"}"));
			}
			if(footer != null) {
				getField(packet.getClass().getDeclaredField("b")).set(packet, this.chatSerializer$a.invoke(null, "{\"text\": \"" + footer + "\"}"));
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
				sendPacket(player, "PacketPlayOutChat", new Class[] { getNMSClass("IChatBaseComponent"), this.chatMessageType }, this.chatSerializer$a.invoke(null, "{\"text\": \"" + message + "\"}"), getField(this.chatMessageType.getDeclaredField("GAME_INFO")).get(null));
			} else if(compareMinecraftVersionServerIsHigherOrEqual("1.8")) {
				sendPacket(player, "PacketPlayOutChat", new Class[] { getNMSClass("IChatBaseComponent"), byte.class }, this.chatSerializer$a.invoke(null, "{\"text\": \"" + message + "\"}"), (byte) 2);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void sendTitle(Player p, String title, String subtitle, int fadein, int stay, int fadeout, boolean clear, boolean reset) {
		title = VariableManager.replace(title, p);
		subtitle = VariableManager.replace(subtitle, p);
		
		try {
			Object t = this.packetPlayOutTitle.newInstance();
			Field f = t.getClass().getDeclaredField("a");
			f.setAccessible(true);
			f.set(t, getField(this.enumTitleAction.getDeclaredField("TITLE")).get(null));
			f = t.getClass().getDeclaredField("b");
			f.setAccessible(true);
			f.set(t, this.chatSerializer$a.invoke(null, "{\"text\": \"" + title + "\"}"));
			sendPacket(p, t);
			
			t = this.packetPlayOutTitle.newInstance();
			f = t.getClass().getDeclaredField("a");
			f.setAccessible(true);
			f.set(t, getField(this.enumTitleAction.getDeclaredField("SUBTITLE")).get(null));
			f = t.getClass().getDeclaredField("b");
			f.setAccessible(true);
			f.set(t, this.chatSerializer$a.invoke(null, "{\"text\": \"" + subtitle + "\"}"));
			sendPacket(p, t);
			
			t = this.packetPlayOutTitle.newInstance();
			f = t.getClass().getDeclaredField("a");
			f.setAccessible(true);
			f.set(t, getField(this.enumTitleAction.getDeclaredField("TIMES")).get(null));
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
				t = this.packetPlayOutTitle.newInstance();
				f = t.getClass().getDeclaredField("a");
				f.setAccessible(true);
				f.set(t, getField(this.enumTitleAction.getDeclaredField("CLEAR")).get(null));
				sendPacket(p, t);
			}
			
			if(reset) {
				t = this.packetPlayOutTitle.newInstance();
				f = t.getClass().getDeclaredField("a");
				f.setAccessible(true);
				f.set(t, getField(this.enumTitleAction.getDeclaredField("RESET")).get(null));
				sendPacket(p, t);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendPacket(Player p, Object packet) {
		try {
			Object nmsPlayer = getNMSPlayer(p);
			Object connection = entityPlayer.getField("playerConnection").get(nmsPlayer);
			playerConnection$sendPacket.invoke(connection, packet);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendPacket(Player p, String packetName, Class<?>[] parameterclass, Object... parameters) {
		try {
			Object nmsPlayer = getNMSPlayer(p);
			Object connection = entityPlayer.getField("playerConnection").get(nmsPlayer);
			Object packet = getNMSClass(packetName).getConstructor(parameterclass).newInstance(parameters);
			connection.getClass().getMethod("sendPacket", this.packet).invoke(connection, packet);
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
	
	private Class<?> getOBCClass(String className) {
		String fullName = "org.bukkit.craftbukkit." + getVersion() + className;
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
