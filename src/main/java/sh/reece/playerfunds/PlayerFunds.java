package sh.reece.playerfunds;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

// TODO: yes, this plugin is highly messy.
// It is no longer maintaned by me, and I do not have time to make it more
// OOP-like. 

public class PlayerFunds extends JavaPlugin {

	private static PlayerFunds instance;

	private Economy econ = null;

	public void onEnable() {
		instance = this;

		if(!setupEconomy()) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		loadConfig();
		new CMD();
	}


	public static PlayerFunds getInstance() {
		return instance;
	}

	public Economy getEconomy() {
		return econ;
	}

	public void onDisable() {
		this.saveDefaultConfig();
		//getServer().getScheduler().cancelTasks(this);		
	}

	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
			Bukkit.getLogger().info("Vault = null");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
			Bukkit.getLogger().info("no RegisteredServiceProvider for economy <Essentials is a good one>");
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
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
