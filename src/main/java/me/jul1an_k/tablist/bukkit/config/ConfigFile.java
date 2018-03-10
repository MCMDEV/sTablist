package me.jul1an_k.tablist.bukkit.config;

import java.io.File;
import java.io.IOException;

import me.jul1an_k.tablist.bukkit.Tablist;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigFile {
	
	private FileConfiguration yaml;
	private File file;
	
	public ConfigFile(String file_path) {
		new ConfigFile("plugins/sTablist", file_path);
	}
	
	public ConfigFile(String folder_path, String file_path) {
		File folder = new File(folder_path);

		if(!folder.isDirectory()) {
			folder.mkdirs();
		}

		file = new File("plugins/sTablist/Prefixes-And-Suffixes", file_path + ".yml");
		
		if(!file.exists()) {
			try {
				file.createNewFile();

				if(file_path.equals("groups"))
				Tablist.getPlugin(Tablist.class).saveResource("Prefixes-And-Suffixes/" + file_path + ".yml", true);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

		yaml = YamlConfiguration.loadConfiguration(file);
	}
	
	public void save() {
		try {
			yaml.save(file);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public FileConfiguration getYaml() {
		return yaml;
	}
	
	public File getFile() {
		return file;
	}
	
}
