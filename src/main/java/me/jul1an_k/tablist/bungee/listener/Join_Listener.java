package me.jul1an_k.tablist.bungee.listener;

import me.jul1an_k.tablist.bungee.Tablist;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.packet.Chat;

public class Join_Listener implements Listener {
	
	@EventHandler
	public void onJoin(PostLoginEvent e) {
		ProxiedPlayer p = e.getPlayer();
		
		String header = replace(Tablist.getInstance().getCfg().getYaml().getString("Tablist.Header"), p);
		String footer = replace(Tablist.getInstance().getCfg().getYaml().getString("Tablist.Footer"), p);
		
		p.setTabHeader(new TextComponent(header), new TextComponent(footer));
		
		if(Tablist.getInstance().getCfg().getYaml().getBoolean("Join.Title.use")) {
			Title bt = ProxyServer.getInstance().createTitle();
			
			bt.title(new TextComponent(replace(Tablist.getInstance().getCfg().getYaml().getString("Join.Title.text"), p)));
			bt.subTitle(new TextComponent(replace(Tablist.getInstance().getCfg().getYaml().getString("Join.Title.subtext"), p)));
			bt.fadeIn(Tablist.getInstance().getCfg().getYaml().getInt("Join.Title.fadein"));
			bt.stay(Tablist.getInstance().getCfg().getYaml().getInt("Join.Title.stay"));
			bt.fadeOut(Tablist.getInstance().getCfg().getYaml().getInt("Join.Title.fadeout"));
			
			p.sendTitle(bt);
		}
		
		if(Tablist.getInstance().getCfg().getYaml().getBoolean("Join.Actionbar.use")) {
			p.unsafe().sendPacket(new Chat(replace(Tablist.getInstance().getCfg().getYaml().getString("Join.Actionbar.text"), p), (byte) 2));
		}
		//
		// if(Tablist.getInstance().getCfg().getYaml().getBoolean("Scoreboard.use"))
		// {
		// Scoreboard sb = new Scoreboard();
		//
		// sb.setName("STLBoard");
		// sb.setPosition(Position.SIDEBAR);
		//
		// p.unsafe().sendPacket(new ScoreboardDisplay((byte) 1, "BOARD"));
		//
		// // CREATE -> 0
		// // DELETE -> 1
		// // UPDATE -> 2
		// p.unsafe().sendPacket(new ScoreboardObjective("Test", (byte) 0, "",
		// null));
		// }
	}
	
	private static String replace(String s, ProxiedPlayer p) {
		s = ChatColor.translateAlternateColorCodes('&', s);
		s = s.replace("%player%", p.getName());
		s = s.replace("%displayname%", p.getDisplayName());
		s = s.replace("%online%", ProxyServer.getInstance().getPlayers().size() + "");
		
		return s;
	}
	
}
