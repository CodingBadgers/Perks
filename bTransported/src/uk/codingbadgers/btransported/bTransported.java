package uk.codingbadgers.btransported;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jnbt.CompoundTag;
import org.jnbt.DoubleTag;
import org.jnbt.ListTag;
import org.jnbt.LongTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.jnbt.Tag;

import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.btransported.commands.CommandSpawn;
import uk.codingbadgers.btransported.commands.CommandWarp;
import uk.codingbadgers.btransported.commands.tp.CommandTP;
import uk.codingbadgers.btransported.commands.tp.CommandTPA;
import uk.codingbadgers.btransported.commands.tp.CommandTPHere;
import uk.codingbadgers.btransported.commands.tp.CommandTPR;
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
		registerCommand(new CommandTPR(this));
		registerCommand(new CommandTPA(this));
		registerCommand(new CommandTPHere(this));
		registerCommand(new CommandWarp(this));
		registerCommand(new CommandSpawn(this));
		
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
			
			m_teleportationConfiguration.addDefault("safe-blocks.blocks", safeBlocks);
			m_teleportationConfiguration.addDefault("safe-blocks.enabled", false);
			
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
	
	/**
	 * Gets the location of a given offline player
	 * @param player	The player of who's location we want
	 * @return	The location if found, null if an error occurred.
	 */
	public Location getLocationOfOfflinePlayer(OfflinePlayer player) {
		
		if (player.isOnline()) {
			return ((Player)player).getLocation();
		}

		final String tpPlayerDatPath = Bukkit.getServer().getWorlds().get(0).getWorldFolder() + "/players/" + player.getName() + ".dat";
		File tpPlayerDat = new File(tpPlayerDatPath);
		if (!tpPlayerDat.exists()) {
			// could not find offline player dat
			log(Level.INFO, "Could not find offline player.dat at " + tpPlayerDatPath);
			return null;
		}
		
		InputStream inputStream = null;
		NBTInputStream nbtInputStream = null;
		
		try {
			inputStream = new FileInputStream(tpPlayerDat);
			nbtInputStream = new NBTInputStream(inputStream);
			
			Tag nbtTag = nbtInputStream.readTag();
			if (!(nbtTag instanceof CompoundTag)) {
				// not a compound tag, this is wrong
				log(Level.INFO, "Root NBT tag was not a compond tag for player '" + tpPlayerDatPath + "'.");
				return null;
			}
			
			CompoundTag rootTag = (CompoundTag)nbtTag;
			List<Tag> position = ((ListTag)rootTag.getValue().get("Pos")).getValue();
			if (position.size() != 3) {
				// there should be 3 elements in the Pos tag
				log(Level.INFO, "The Pos tag of the player '" + tpPlayerDatPath + "' does not have 3 coordinates.");
				return null;
			}
			
			Double x = (Double)position.get(0).getValue();
			Double y = (Double)position.get(1).getValue();
			Double z = (Double)position.get(2).getValue();
			
			Long worldLeast = ((LongTag)rootTag.getValue().get("WorldUUIDLeast")).getValue();
			Long worldMost = ((LongTag)rootTag.getValue().get("WorldUUIDMost")).getValue();
			World world = Bukkit.getWorld(new UUID(worldMost, worldLeast));
			
			return new Location(world, x, y, z);					
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {	
			try {
				nbtInputStream.close();
				inputStream.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
	}
	
	/**
	 * Teleport an offline player to a given location
	 * @return True on success false otherwise
	 */	
	public boolean teleportOfflinePlayer(OfflinePlayer player, Location location) {
	
		// Get the player dat of the player we want to modify
		final String tpPlayerDatPath = Bukkit.getServer().getWorlds().get(0).getWorldFolder() + "/players/" + player.getName() + ".dat";
		File tpPlayerDat = new File(tpPlayerDatPath);
		if (!tpPlayerDat.exists()) {
			// could not find offline player dat
			this.log(Level.INFO, "Could not find offline player.dat at " + tpPlayerDatPath);
			return false;
		}
		
		File tpPlayerBak = new File(Bukkit.getServer().getWorlds().get(0).getWorldFolder() + "/players/" + player.getName() + ".bak");
		if (tpPlayerBak.exists()) {
			tpPlayerBak.delete();
		}
		
		tpPlayerDat.renameTo(tpPlayerBak);
		
		InputStream inputStream = null;
		OutputStream outputStream = null;
		NBTInputStream nbtInputStream = null;
		NBTOutputStream nbtOutputStream = null;
		
		try {
			inputStream = new FileInputStream(tpPlayerBak);
			nbtInputStream = new NBTInputStream(inputStream);
			
			outputStream = new FileOutputStream(tpPlayerDat);
			nbtOutputStream = new NBTOutputStream(outputStream);
			
			Tag nbtTag = nbtInputStream.readTag();
			if (!(nbtTag instanceof CompoundTag)) {
				// not a compound tag, this is wrong
				this.log(Level.INFO, "Root NBT tag was not a compond tag for player '" + tpPlayerDatPath + "'.");
				return false;
			}
			
			CompoundTag rootTag = (CompoundTag)nbtTag;
			List<Tag> position = ((ListTag)rootTag.getValue().get("Pos")).getValue();
			if (position.size() != 3) {
				// there should be 3 elements in the Pos tag
				this.log(Level.INFO, "The Pos tag of the player '" + tpPlayerDatPath + "' does not have 3 coordinates.");
				return false;
			}

			Long worldLeast = ((LongTag)rootTag.getValue().get("WorldUUIDLeast")).getValue();
			Long worldMost = ((LongTag)rootTag.getValue().get("WorldUUIDMost")).getValue();
			World currentworld = Bukkit.getWorld(new UUID(worldMost, worldLeast));
			
			Tag newX = new DoubleTag(position.get(0).getName(), location.getX());
			Tag newY = new DoubleTag(position.get(1).getName(), location.getY());
			Tag newZ = new DoubleTag(position.get(2).getName(), location.getZ());
			
			position.set(0, newX);
			position.set(1, newY);
			position.set(2, newZ);
			
			// if the offline player is currently in the wrong world, switch the world aswell
			if (!currentworld.getName().equalsIgnoreCase(location.getWorld().getName())) {
				
				// Get the world UID information
				World world = location.getWorld();
				Long leastValue = world.getUID().getLeastSignificantBits();
				Long mostValue = world.getUID().getMostSignificantBits();
				
				// create new tags
				Tag newWorldLeast = new LongTag(((LongTag)rootTag.getValue().get("WorldUUIDLeast")).getName(), leastValue);
				Tag newWorldMost = new LongTag(((LongTag)rootTag.getValue().get("WorldUUIDMost")).getName(), mostValue);
			
				// remove old tags
				rootTag.getValue().remove("WorldUUIDLeast");
				rootTag.getValue().remove("WorldUUIDMost");
				
				// add new tags
				rootTag.getValue().put("WorldUUIDLeast", newWorldLeast);
				rootTag.getValue().put("WorldUUIDMost", newWorldMost);
				
			}
			
			nbtOutputStream.writeTag(nbtTag);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		} finally {	
			try {
				nbtInputStream.close();
				inputStream.close();
				nbtOutputStream.close();
				outputStream.flush();
				outputStream.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				
				tpPlayerDat.delete();
				tpPlayerBak.renameTo(tpPlayerDat);
								
				return false;
			}
		}
		
		return true;
	}
}
