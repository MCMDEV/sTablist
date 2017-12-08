package me.jul1an_k.tablist.bukkit.scoreboard;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ScoreboardConfig {

	public FileConfiguration YAML;
	public File FILE;
	
	public ScoreboardConfig() {
		FILE = new File("plugins/sTablist", "scoreboard.yml");
		YAML = YamlConfiguration.loadConfiguration(FILE);
	}
	
}
