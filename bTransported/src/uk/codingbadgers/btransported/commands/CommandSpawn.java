package uk.codingbadgers.btransported.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.btransported.bTransported;
import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;

/**
 * @author N3wton
 * The spawn command allows users to telelport themselves to the spawn point of a world
 * They can teleport themselves to any world or their current world spawn
 * They can teleport other players to their current world or any other world spawn
 * All based around permissions.
 * There is also the ability to teleport offline players to a world spawn
 */
public class CommandSpawn extends ModuleCommand {
	
	/** The bFundamentals module */
	private bTransported m_module = null;
	
	/** A hash map of world spawns. The String is the world name, the Location is the location of the spawn */
	private HashMap<String, Location> m_spawn = new HashMap<String, Location>();

	/**
	 * Class constructor
	 * @param module	The bFundamentals module
	 */
	public CommandSpawn(bTransported module) {
		super("spawn", "spawn | spawn <world name> | spawn <player_name> | spawn <world_name> <player_name> | spawn set | spawn delete <world_name>");
		m_module = module;
		
		createDatabase();
		loadSpawnsFromDatabase();
	}

	@Override
	/**
	 * Called when a command is executed. Returning true indicates the command was handled.
	 * @param sender	The thing that executed the command, player or console.
	 * @param label 	The label of the command
	 * @param args		The arguments passed with command
	 */
	public boolean onCommand(CommandSender sender, String label, String[] args)
	{
		// /Spawn cannot be used via the console currently.
		if (!(sender instanceof Player)) {
			return true;
		}
		
		// Cast the sender to a player
		final Player player = (Player)sender;
		
		// Make sure the sending player has the spawn permission
		if (!Module.hasPermission(player, "perks.btransported.spawn")) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-NO-PERMISSION").replace("%permission%", "perks.btransported.spawn"));
			return true;
		}
				
		// Handle /spawn
		if (args.length == 0) {
		
			if (!Module.hasPermission(player, "perks.btransported.spawn")) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-NO-PERMISSION").replace("%permission%", "perks.btransported.spawn"));
				return true;
			}
			
			final String worldname = player.getWorld().getName();
			final String tpMessage = m_module.getLanguageValue("COMMAND-SPAWN-COMPLETE").replace("%worldname%", worldname);
			
			teleportPlayer(player, worldname, tpMessage);
			
			return true;
			
		// Handle /spawn <world name> and /spawn <player name>
		} else if (args.length == 1) {
			
			if (args[0].equalsIgnoreCase("set")) {
				
				final Location location = player.getLocation();
				final String worldName = location.getWorld().getName();
				
				if (m_spawn.containsKey(worldName)) {
					Module.sendMessage("bTransported", player.getPlayer(), m_module.getLanguageValue("COMMAND-SPAWN-WORLD-ALREADY-EXISTS").replace("%worldname%", worldName));					
					return true;
				}
				
				m_spawn.put(worldName, location);
				addSpawnToDatabase(location);
				
				Module.sendMessage("bTransported", player.getPlayer(), m_module.getLanguageValue("COMMAND-SPAWN-ADDED").replace("%worldname%", worldName));
				
				return true;
				
			} else if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) { 
				
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-USAGE"));
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-ONE-PARAM-USAGE"));
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-TWO-PARAM-USAGE"));
				
			} else {
			
				final String name = args[0];
							
				World world = Bukkit.getWorld(name);
				// teleport the player who said the command to the spawn of the given world	
				if (world != null) {
					if (!Module.hasPermission(player, "perks.btransported.spawn.world")) {
						Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-NO-PERMISSION").replace("%permission%", "perks.btransported.spawn.world"));
						return true;
					}
					
					final String worldname = world.getName();
					final String tpMessage = m_module.getLanguageValue("COMMAND-SPAWN-COMPLETE").replace("%worldname%", worldname);
										
					teleportPlayer(player, worldname, tpMessage);
					
					return true;
				}
				
				OfflinePlayer tpPlayer = Bukkit.getOfflinePlayer(name);
				// teleport the given player to the spawn of the command players world
				if (tpPlayer.hasPlayedBefore()) {
					if (!Module.hasPermission(player, "perks.btransported.spawn.other")) {
						Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-NO-PERMISSION").replace("%permission%", "perks.btransported.spawn.other"));
						return true;
					}
					
					final String worldname = player.getWorld().getName();
					final String tpMessage = m_module.getLanguageValue("COMMAND-SPAWN-COMPLETE").replace("%worldname%", worldname);
										
					if (teleportPlayer(tpPlayer, worldname, tpMessage)) {					
						Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-COMPLETE-OTHER").replace("%worldname%", worldname).replace("%playername%", tpPlayer.getName()));
					} else {
						Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-FAILED"));
					}
					
					return true;
				}
				
				// not a world or player
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-PLAYER-WORLD-NOT-FOUND").replace("%name%", name));
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-ONE-PARAM-USAGE"));
			}
			
			return true;
			
		// handle /spawn <worldname> <playername>
		} else if (args.length == 2) {
			
			if (args[0].equalsIgnoreCase("delete")) {
				
				final String worldName = args[1];
				
				if (!m_spawn.containsKey(worldName)) {
					Module.sendMessage("bTransported", player.getPlayer(), m_module.getLanguageValue("COMMAND-SPAWN-WORLD-DOESNT-EXIST").replace("%worldname%", worldName));					
					return true;
				}
				
				m_spawn.remove(worldName);
				deleteSpawnFromDatabase(worldName);
				
				Module.sendMessage("bTransported", player.getPlayer(), m_module.getLanguageValue("COMMAND-SPAWN-DELETED").replace("%worldname%", worldName));
				
				return true;
			}

			if (!Module.hasPermission(player, "perks.btransported.spawn.other.world")) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-NO-PERMISSION").replace("%permission%", "perks.btransported.spawn.other.world"));
				return true;
			}
			
			final String playername = args[0];
			final String worldname = args[1];
						
			OfflinePlayer tpPlayer = Bukkit.getOfflinePlayer(playername);
			if (!tpPlayer.hasPlayedBefore()) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-PLAYER-NOT-FOUND").replace("%playername%", playername));
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-TWO-PARAM-USAGE"));
				return true;
			}

			final String tpMessage = m_module.getLanguageValue("COMMAND-SPAWN-COMPLETE").replace("%worldname%", worldname);
			
			if (teleportPlayer(tpPlayer, worldname, tpMessage)) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-COMPLETE-OTHER").replace("%worldname%", worldname).replace("%playername%", tpPlayer.getName()));
			} else {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-FAILED"));
			}
				
			return true;
		}
		
		return false;
	}
	
	/**
	 * Teleport a given player to a given world spawn
	 * @param player		The player to teleport
	 * @param worldname		The name of the world spawn to teleport too
	 * @return				True if teleported successfully, false otherwise.
	 */
	private boolean teleportPlayer(OfflinePlayer player, String worldname, String teleportMessage) {
		
		Location location = null;
		
		if (!m_spawn.containsKey(worldname)) {
			
			World world = Bukkit.getWorld(worldname);
			if (world == null) {
				if (player.isOnline()) {
					Module.sendMessage("bTransported", player.getPlayer(), m_module.getLanguageValue("COMMAND-SPAWN-WORLD-NOT-FOUND").replace("%worldname%", worldname));					
				}
				return false;
			}
			location = world.getSpawnLocation();
		}
		else
		{
			location = m_spawn.get(worldname);
		}
		
		if (location == null) {
			// this should be impossible
			return false;
		}
		
		if (player.isOnline()) {
			if (player.getPlayer().teleport(location)) {	
				Module.sendMessage("bTransported", player.getPlayer(), teleportMessage);
			}
			return true;
		}
		
		// player is offline, edit there nbt data
		if (!m_module.teleportOfflinePlayer(player, location)) {
			return false;			
		}
			
		return true;		
	}
	
	/**
	 * Adds a world spawn to the database
	 * @param location		The location to add
	 */
	private void addSpawnToDatabase(final Location location) {
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		
		String query = "INSERT INTO `perks_spawn` " +
				"(`world`,`x`,`y`,`z`,`yaw`,`pitch`) VALUES (" + 
				"'" + location.getWorld().getName() + "'," +
				"'" + location.getX() + "'," +
				"'" + location.getY() + "'," +
				"'" + location.getZ() + "'," +
				"'" + location.getYaw() + "'," +
				"'" + location.getPitch() + 
				"');";
		
		db.Query(query);
	}
	
	/**
	 * Delete a world spawn from the database
	 * @param worldName		The name of the world spawn to be deleted.
	 */
	private void deleteSpawnFromDatabase(final String worldName) {
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		
		String query = "DELETE FROM `perks_spawn` " +
				"WHERE world=" + "'" + worldName + "';";
		
		db.Query(query, true);
	}
	
	/**
	 * Create the spawn database table should it not exist
	 */
	private void createDatabase() {
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		if (!db.TableExists("perks_spawn")) {			
			String query = "CREATE TABLE perks_spawn (" +
					"world VARCHAR(128)," +
					"x INT," +
					"y INT," +
					"z INT," +
					"yaw INT," +
					"pitch INT" +
					");";
			
			db.Query(query, true);
		}
	}
	
	/**
	 * Load all the spawns out of the database into a hash map, saving a database lookup on command execution.
	 */
	private void loadSpawnsFromDatabase() {
		
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		
		String query = "SELECT * FROM perks_spawn";
		ResultSet result = db.QueryResult(query);
		
		if (result != null) {
			try {
				// while we have another result, read in the data
				while (result.next()) {
		            String worldName = result.getString("world");
	
		            int x = result.getInt("x");
		            int y = result.getInt("y");
		            int z = result.getInt("z");
		            int pitch = result.getInt("pitch");
		            int yaw = result.getInt("yaw");
		            
		            World world = Bukkit.getServer().getWorld(worldName);
		            if (world == null) {
		            	bFundamentals.log(Level.SEVERE, "A world spawn for the world '" + worldName + "' could not be loaded as that world does not exist!");
		            	continue;
		            }
		            
		            Location location = new Location(world, x, y, z, yaw, pitch);        
		            m_spawn.put(worldName, location);		            
		        }
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}
			
			db.FreeResult(result);
		}
	}
	
}
