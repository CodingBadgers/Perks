package uk.codingbadgers.btransported;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.btransported.commands.tp.CommandTP;
import uk.codingbadgers.btransported.listeners.PlayerTeleportListener;

/**
 * The Class bTransported.
 * Main entry point to the module
 */
public class bTransported extends Module {
	
	private FileConfiguration m_teleportationConfiguration = null;
	
	/**
	 * This is called when the module is unloaded
	 */
	@Override
	public void onDisable() {
		log(Level.INFO,  getName() + " version " + getVersion() + " disabled.");
	}

	/**
	 * Called when the module is loaded.
	 * Allowing us to register the player and block listeners
	 */
	@Override
	public void onEnable() {	
		
		loadLanguageFile();
		createConfigurationFiles();
		
		// Resiter the commands
		registerCommand(new CommandTP(this));
		
		// Register the player teleport listener
		register(new PlayerTeleportListener(this));
		
		log(Level.INFO,  getName() + " version " + getVersion() + " enabled.");
	}

	/**
	 * 
	 */
	private void createConfigurationFiles() {
		
		final File folder = this.getDataFolder();
		
		// create the dangerous block configuration file
		createTeleportConfiguration(folder);
		
	}
	
	/**
	 * 
	 */
	private void createTeleportConfiguration(final File folder)
	{
		File configFile = new File(folder + File.separator + "teleporting.yml");
		
		// If the configuration file doesnt exist, make a new one and populate it with default values
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException exception) {
				log(Level.SEVERE, "Failed to create teleporting.yml");
				exception.printStackTrace();
				return;
			}
			
			// Create a Yaml configuration for the given config file
			m_teleportationConfiguration = YamlConfiguration.loadConfiguration(configFile);
			
			// Add the default blocks
			List<Integer> blackListedBlocks = new ArrayList<Integer>();
			blackListedBlocks.add(Material.LAVA.getId());
			blackListedBlocks.add(Material.CACTUS.getId());
			blackListedBlocks.add(Material.FIRE.getId());
						
			m_teleportationConfiguration.addDefault("dangerous-blocks", blackListedBlocks);
			
			List<Integer> safeBlocks = new ArrayList<Integer>();
			safeBlocks.add(Material.AIR.getId());
			safeBlocks.add(Material.WATER.getId());
			safeBlocks.add(Material.SIGN.getId());
			safeBlocks.add(Material.SIGN_POST.getId());
			safeBlocks.add(Material.THIN_GLASS.getId());
			safeBlocks.add(Material.LONG_GRASS.getId());
			safeBlocks.add(Material.DEAD_BUSH.getId());
			safeBlocks.add(Material.RED_ROSE.getId());
			safeBlocks.add(Material.YELLOW_FLOWER.getId());
			safeBlocks.add(Material.SAPLING.getId());
			safeBlocks.add(Material.RAILS.getId());
			safeBlocks.add(Material.ACTIVATOR_RAIL.getId());
			safeBlocks.add(Material.DETECTOR_RAIL.getId());
			safeBlocks.add(Material.POWERED_RAIL.getId());
			
			m_teleportationConfiguration.addDefault("safe-blocks", safeBlocks);
			
			m_teleportationConfiguration.addDefault("maximum-fall-distance", 5);

			List<String> teleportcommandworlds = new ArrayList<String>();
			m_teleportationConfiguration.addDefault("disable-in-world.teleport-command", teleportcommandworlds);

			List<String> teleportenderpearlworlds = new ArrayList<String>();
			teleportenderpearlworlds.add("world");
			m_teleportationConfiguration.addDefault("disable-in-world.teleport-enderpearl", teleportenderpearlworlds);
			
			List<String> teleportendportalworlds = new ArrayList<String>();
			m_teleportationConfiguration.addDefault("disable-in-world.teleport-endportal", teleportendportalworlds);
			
			List<String> teleportnetherportalworlds = new ArrayList<String>();
			m_teleportationConfiguration.addDefault("disable-in-world.teleport-neatherportal", teleportnetherportalworlds);
			
			List<String> teleportpluginworlds = new ArrayList<String>();
			m_teleportationConfiguration.addDefault("disable-in-world.teleport-plugin", teleportpluginworlds);
			
			// Save the changes
			m_teleportationConfiguration.options().copyDefaults(true);
			try { 
				m_teleportationConfiguration.save(configFile); 
			} catch (IOException exception) {
				log(Level.SEVERE, "Failed to save teleporting.yml");
				exception.printStackTrace();
				return;
			}
			
			log(Level.INFO, "Created the teleporting configuration file.");
		}
		else {
			// configuration already exists, so just load that one
			m_teleportationConfiguration = YamlConfiguration.loadConfiguration(configFile);
		}
		
	}

	/**
	 * Get the dangerous block configuration file
	 * @return The dangerous block configuration file
	 */
	public FileConfiguration getTeleportationConfig() {
		return m_teleportationConfiguration;
	}
}
