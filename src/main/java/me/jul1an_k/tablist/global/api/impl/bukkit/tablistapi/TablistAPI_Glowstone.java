package me.jul1an_k.tablist.global.api.impl.bukkit.tablistapi;

import org.bukkit.entity.Player;

import me.jul1an_k.tablist.bukkit.sTablistAPI;
import me.jul1an_k.tablist.bukkit.variables.VariableManager;
import net.glowstone.entity.GlowPlayer;
import net.md_5.bungee.api.chat.TextComponent;

public class TablistAPI_Glowstone extends sTablistAPI {
	
	public void sendTabList(Player player, String header, String footer) {
		if(header != null)
			header = VariableManager.replaceTab(header, player);
		
		if(footer != null)
			footer = VariableManager.replaceTab(footer, player);
		
		((GlowPlayer) player).setPlayerListHeaderFooter(new TextComponent(header), new TextComponent(footer));
	}
	
	public void sendActionBar(Player player, String message) {
		if(message != null)
			message = VariableManager.replace(message, player);
		
		((GlowPlayer) player).sendActionBar(message);
	}
	
	public void sendTitle(Player player, String title, String subtitle, int fadein, int stay, int fadeout, boolean clear, boolean reset) {
		title = VariableManager.replace(title, player);
		subtitle = VariableManager.replace(subtitle, player);
		
		GlowPlayer glowPlayer = (GlowPlayer) player;
		
		glowPlayer.sendTitle(title, subtitle, fadein, stay, fadeout);
		
		if(clear)
			glowPlayer.clearTitle();
		
		if(reset)
			glowPlayer.resetTitle();
	}
	
	public int getPing(Player player) {
		//TODO: Fix me
		return 0;
	}
	
	public String getVersion() {
		return "Glowstone";
	}
	
}
