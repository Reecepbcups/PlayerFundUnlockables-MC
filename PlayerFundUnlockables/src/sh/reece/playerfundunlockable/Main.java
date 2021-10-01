package sh.reece.playerfundunlockable;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	public void onEnable() {
		loadConfig();
		
		new CMD(this);
	}

	public void onDisable() {
		this.saveDefaultConfig();
		//getServer().getScheduler().cancelTasks(this);		
	}
	
	
	// config work
	public void loadConfig() {		
		createConfig("config.yml");		
		createFile("data.yml");
		getConfig().options().copyDefaults(true);	
	}
	
	public FileConfiguration getConfigFile(String name) {
		return YamlConfiguration.loadConfiguration(new File(getDataFolder(), name));
	}

	public void createDirectory(String DirName) {
		File newDir = new File(getDataFolder(), DirName.replace("/", File.separator));
		if (!newDir.exists()){
			newDir.mkdirs();
		}
	}

	public void createConfig(String name) {
		File file = new File(getDataFolder(), name);

		if (!new File(getDataFolder(), name).exists()) {

			saveResource(name, false);
		}

		@SuppressWarnings("static-access")
		FileConfiguration configuration = new YamlConfiguration().loadConfiguration(file);
		if (!file.exists()) {
			try {
				configuration.save(file);
			}			
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void createFile(String name) {
		File file = new File(getDataFolder(), name);

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void saveConfig(FileConfiguration config, String name) {
		try {
			config.save(new File(getDataFolder(), name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
