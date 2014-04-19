package uk.thecodingbadgers.binvested;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.binvested.commands.*;
import uk.thecodingbadgers.binvested.listeners.*;

public class bInvested extends Module {

	private static bInvested instance = null;
	
	private YamlConfiguration m_settings = null;
	
	public static bInvested getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;
		
		loadLanguageFile();
		loadConfig();
		
		if (m_settings.getBoolean("commands.fly")) {
			registerCommand(new CommandFly());
			register(new FlyListener(m_database));
		}
		
		if (m_settings.getBoolean("commands.workbench")) {
			registerCommand(new CommandCraft());
		}
		
		if (m_settings.getBoolean("commands.ender")) {
			registerCommand(new CommandEnder());
		}
		
		if (m_settings.getBoolean("commands.anvil")) {
			registerCommand(new CommandAnvil());
		}
		
		if (m_settings.getBoolean("commands.enchanting")) {
			registerCommand(new CommandEnchant());
		}
		
		if (m_settings.getBoolean("perk.scuba")) {
			register(new ScubaListener());
		}
		
		if (m_settings.getBoolean("perk.hunger")) {
			register(new HungerListener(getConfig().getDouble("hunger.rate", 0.25f)));
		}
		
		log(Level.INFO, getName() + " version " + getVersion() + " has been enabled successfully");
	}
	
	/**
	 * 
	 */
	private void loadConfig() {
		
		File configFile = new File(this.getDataFolder() + File.separator + "config.yml");
		
		// If the configuration file doesnt exist, make a new one and populate it with default values
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException exception) {
				log(Level.SEVERE, "Failed to create config.yml");
				exception.printStackTrace();
				return;
			}
			
			// Create a Yaml configuration for the given config file
			m_settings = YamlConfiguration.loadConfiguration(configFile);
			
			m_settings.addDefault("commands.fly", true);
			m_settings.addDefault("commands.workbench", true);
			m_settings.addDefault("commands.anvil", true);
			m_settings.addDefault("commands.enchanting", true);
			m_settings.addDefault("commands.ender", true);
			m_settings.addDefault("perk.scuba", true);
			m_settings.addDefault("perk.hunger", true);
			
			try {
				m_settings.save(configFile);
			} catch (Exception ex) {
				log(Level.SEVERE, "Failed to create config.yml");
				ex.printStackTrace();
			}
			
			log(Level.INFO, "Created the bInvested configuration file.");
		}
		else {
			// configuration already exists, so just load that one
			m_settings = YamlConfiguration.loadConfiguration(configFile);
		}
		
	}

	@Override
	public void onDisable() {
		log(Level.INFO, getName() + " version " + getVersion() + " has been disabled successfully");
	}
}
