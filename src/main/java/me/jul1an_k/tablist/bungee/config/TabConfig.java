package me.jul1an_k.tablist.bungee.config;

import java.io.File;
import java.io.IOException;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

public class TabConfig {
	
	private Configuration yaml;
	private File file;
	
	public TabConfig() {
		file = new File("plugins/sTablist", "config.yml");
		File folder = new File("plugins/sTablist");
		if(!folder.exists()) {
			folder.mkdir();
		}
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		try {
			yaml = YamlConfiguration.getProvider(YamlConfiguration.class).load(file);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		if(!yaml.getKeys().contains("Tablist.Header")) {
			yaml.set("Tablist.Header", "&5This is the Header!");
		}
		
		if(!yaml.getKeys().contains("Tablist.Footer")) {
			yaml.set("Tablist.Footer", "&5This is the Footer!");
		}
		
		if(!yaml.getKeys().contains("Join.Title.text")) {
			yaml.set("Join.Title.text", "&5Welcome to");
		}
		
		if(!yaml.getKeys().contains("Join.Title.subtext")) {
			yaml.set("Join.Title.subtext", "&5A Minecraft Server!");
		}
		
		if(!yaml.getKeys().contains("Join.Title.fadein")) {
			yaml.set("Join.Title.fadein", 5);
		}
		
		if(!yaml.getKeys().contains("Join.Title.stay")) {
			yaml.set("Join.Title.stay", 20);
		}
		
		if(!yaml.getKeys().contains("Join.Title.fadeout")) {
			yaml.set("Join.Title.fadeout", 5);
		}
		
		if(!yaml.getKeys().contains("Join.Title.use")) {
			yaml.set("Join.Title.use", true);
		}
		
		if(!yaml.getKeys().contains("Join.Actionbar.text")) {
			yaml.set("Join.Actionbar.text", "Have fun!");
		}
		
		if(!yaml.getKeys().contains("Join.Actionbar.use")) {
			yaml.set("Join.Actionbar.use", true);
		}
		
		if(!yaml.getKeys().contains("Updater.Enable")) {
			yaml.set("Updater.Enable", true);
		}
		
		try {
			YamlConfiguration.getProvider(YamlConfiguration.class).save(yaml, file);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public Configuration getYaml() {
		return yaml;
	}
	
	public File getFile() {
		return file;
	}
	
}
