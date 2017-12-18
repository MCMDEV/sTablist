package me.jul1an_k.tablist.global.api.impl.bukkit.tablistapi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.jul1an_k.tablist.bukkit.sTablistAPI;
import me.jul1an_k.tablist.bukkit.variables.VariableManager;

public class TablistAPI_ProtocolHack extends sTablistAPI {

	private Class<?> chatSerializer;
	private Class<?> packetPlayOutTitle;
	private Class<?> enumTitleAction;
	private Class<?> packet;
	private Class<?> craftPlayer;
	private Class<?> entityPlayer;
	private Class<?> playerConnection;

	private Method chatSerializer$a;
	private Method craftPlayer$getHandle;
	private Method playerConnection$sendPacket;
	
	public TablistAPI_ProtocolHack() {
		this.chatSerializer = getNMSClass("ChatSerializer");
		this.packetPlayOutTitle = getProtocolInjectorClass("PacketTitle");
		this.enumTitleAction = getProtocolInjectorClass("PacketTitle$Action");

		this.packet = getNMSClass("Packet");
		this.craftPlayer = getOBCClass("CraftPlayer");
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
			
			Object packet = getProtocolInjectorClass("PacketTabHeader").newInstance();
			
			if(header != null)
				getField(packet.getClass().getDeclaredField("header")).set(packet, this.chatSerializer$a.invoke(null, "{\"text\": \"" + header + "\"}"));
			
			if(footer != null)
				getField(packet.getClass().getDeclaredField("footer")).set(packet, this.chatSerializer$a.invoke(null, "{\"text\": \"" + footer + "\"}"));
			
			sendPacket(player, packet);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendActionBar(Player player, String message) {
		if(message != null)
			message = VariableManager.replace(message, player);
		
		try {
			sendPacket(player, "PacketPlayOutChat", new Class[] { getNMSClass("IChatBaseComponent"), byte.class }, this.chatSerializer$a.invoke(null, "{\"text\": \"" + message + "\"}"), (byte) 2);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendTitle(Player player, String title, String subtitle, int fadein, int stay, int fadeout, boolean clear, boolean reset) {
		title = VariableManager.replace(title, player);
		subtitle = VariableManager.replace(subtitle, player);
		
		try {
			Object t = this.packetPlayOutTitle.newInstance();
			Field f = t.getClass().getDeclaredField("action");
			f.setAccessible(true);
			f.set(t, getField(this.enumTitleAction.getDeclaredField("TITLE")).get(null));
			f = t.getClass().getDeclaredField("text");
			f.setAccessible(true);
			f.set(t, this.chatSerializer$a.invoke(null, "{\"text\": \"" + title + "\"}"));
			sendPacket(player, t);
			
			t = this.packetPlayOutTitle.newInstance();
			f = t.getClass().getDeclaredField("action");
			f.setAccessible(true);
			f.set(t, getField(this.enumTitleAction.getDeclaredField("SUBTITLE")).get(null));
			f = t.getClass().getDeclaredField("text");
			f.setAccessible(true);
			f.set(t, this.chatSerializer$a.invoke(null, "{\"text\": \"" + subtitle + "\"}"));
			sendPacket(player, t);
			
			t = this.packetPlayOutTitle.newInstance();
			f = t.getClass().getDeclaredField("action");
			f.setAccessible(true);
			f.set(t, getField(this.enumTitleAction.getDeclaredField("TIMES")).get(null));
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
				t = this.packetPlayOutTitle.newInstance();
				f = t.getClass().getDeclaredField("action");
				f.setAccessible(true);
				f.set(t, getField(this.enumTitleAction.getDeclaredField("CLEAR")).get(null));
				sendPacket(player, t);
			}
			
			if(reset) {
				t = this.packetPlayOutTitle.newInstance();
				f = t.getClass().getDeclaredField("action");
				f.setAccessible(true);
				f.set(t, getField(this.enumTitleAction.getDeclaredField("RESET")).get(null));
				sendPacket(player, t);
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
