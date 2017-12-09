package me.jul1an_k.tablist.bukkit.scoreboard;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.jul1an_k.tablist.bukkit.sTablistAPI;
import me.jul1an_k.tablist.bukkit.variables.VariableManager;

public class STLScoreboard {
	
	private static ScoreboardConfig sbcfg = new ScoreboardConfig();
	
	private static FileConfiguration fc = sbcfg.YAML;
	
	private static int currentScore = 0;
	
	private static Map<Player, Scoreboard> boards = new HashMap<>();
	
	private static void addScore(Objective obj, String name) {
		obj.getScore(name).setScore(currentScore);
		currentScore--;
	}
	
	public static void show(Player p) {
		Scoreboard board = boards.containsKey(p) ? boards.get(p) : boards.put(p, Bukkit.getScoreboardManager().getNewScoreboard());
		board.clearSlot(DisplaySlot.SIDEBAR);
		Objective obj = board.getObjective(p.getName()) == null ? board.registerNewObjective(p.getName(), p.getName()) : board.getObjective(p.getName());
		
		obj.unregister();
		p.sendMessage("unregister done.");
		currentScore = 0;
		
		board = Bukkit.getScoreboardManager().getMainScoreboard();
		obj = board.getObjective(p.getName()) == null ? board.registerNewObjective(p.getName(), p.getName()) : board.getObjective(p.getName());
		
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(VariableManager.replace(fc.getString("DisplayName"), p));
		
		for(String s : fc.getStringList("Lines")) {
			String replaced = VariableManager.replace(s, p);
			
			if(sTablistAPI.getImpl().compareMinecraftVersionServerIsHigherOrEqual("1.8")) {
				if(replaced.length() > 40) {
					replaced = replaced.substring(replaced.length());
				}
			} else if(sTablistAPI.getImpl().compareMinecraftVersionServerIsHigherOrEqual("1.7.10")) {
				if(replaced.length() > 16) {
					replaced = replaced.substring(replaced.length());
				}
			}
			
			addScore(obj, VariableManager.replace(replaced, p));
		}
		
		p.setScoreboard(board);
	}
	
	public static Scoreboard getBoard(Player p) {
		return boards.get(p);
	}
	
}
