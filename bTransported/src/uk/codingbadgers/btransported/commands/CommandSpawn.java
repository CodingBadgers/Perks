package uk.codingbadgers.btransported.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.gui.GuiInventory;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.btransported.bTransported;
import uk.codingbadgers.btransported.commands.callbacks.SpawnGuiCallback;
import uk.codingbadgers.btransported.permissions.SpawnPermission;
import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;

/**
 * @author N3wton
 * The spawn command allows users to telelport themselves to the spawn point of a world
 * They can teleport themselves to any world or their current world spawn
 * They can teleport other players to their current world or any other world spawn
 * All based around permissions.
 * There is also the ability to teleport offline players to a world spawn
 */
public class CommandSpawn extends CommandPlaceBase {
	
	/** The bFundamentals module */
	private final bTransported m_module;
	
	/** A hash map of world spawns. The String is the world name, the Location is the location of the spawn */
	private final HashMap<String, Location> m_spawn = new HashMap<String, Location>();

	/**
	 * Class constructor
	 * @param module	The bFundamentals module
	 */
	public CommandSpawn(bTransported module) {
		super(
			module, 
			"BF-SPAWN_ANVIL", 
			"spawn", 
			"spawn | spawn <world name> | spawn <player name> | spawn <player name> <world name> | spawn set | spawn remove | spawn <world name> remove"
		);
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
				
		// Handle /spawn
		if (args.length == 0) {
		
			if (!Module.hasPermission(player, SpawnPermission.Gui.permission)) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-NO-PERMISSION").replace("%permission%", SpawnPermission.Gui.permission));
				return true;
			}
			
			handleSpawnGUI(player);
			
			return true;
			
		} else if (args.length == 1) {
			
			// Handle /spawn set
			if (args[0].equalsIgnoreCase("set")) {
				
				handleSpawnSet(player);
				return true;
				
			// Handle /spawn remove
			} else if (args[0].equalsIgnoreCase("remove")) {
				
				handleSpawnRemove(player, player.getWorld().getName());
				return true;
				
			// Handle /spawn help
			} else if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) { 
				
				handleSpawnHelp(player);
				return true;

			}
			
			// Handle /spawn <worldname>
			final String name = args[0];
			if (Bukkit.getWorld(name) != null) {
				handleSpawnWorldName(player, player.getName(), name);
				return true;
			}

			// Handle /spawn <playername>
			OfflinePlayer tpPlayer = Bukkit.getOfflinePlayer(name);
			// teleport the given player to the spawn of the command players world
			if (tpPlayer.hasPlayedBefore()) {
				handleSpawnWorldName(player, tpPlayer.getName(), player.getWorld().getName());
				return true;
			}

			// not a world or player
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-PLAYER-WORLD-NOT-FOUND").replace("%name%", name));
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-ONE-PARAM-USAGE"));
			return true;

		} else if (args.length == 2) {
			
			// Handle /spawn <world name> remove
			if (args[1].equalsIgnoreCase("remove")) {
				handleSpawnRemove(player, args[0]);
				return true;
			}

			// Handle /spawn <player name> <world name>
			if (!Module.hasPermission(player, SpawnPermission.OtherWorld.permission)) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-NO-PERMISSION").replace("%permission%", SpawnPermission.OtherWorld.permission));
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
	 * Handle /spawn <worldname> and /spawn <playername> <worldname>
	 * @param player
	 * @param playername
	 * @param worldname 
	 */
	private void handleSpawnWorldName(Player player, String playername, String worldname) {
		
		if (!Module.hasPermission(player, SpawnPermission.Spawn.permission + "." + worldname)) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-NO-PERMISSION").replace("%permission%", SpawnPermission.Spawn.permission + "." + worldname));
			return;
		}
		
		// teleporting a different player
		if (!player.getName().equalsIgnoreCase(playername)) {
			if (!Module.hasPermission(player, SpawnPermission.Other.permission)) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-NO-PERMISSION").replace("%permission%", SpawnPermission.Other.permission));
				return;
			}
		}

		final String tpMessage = m_module.getLanguageValue("COMMAND-SPAWN-COMPLETE").replace("%worldname%", worldname);
		teleportPlayer(player, worldname, tpMessage);
	}
	
	/**
	 * Handle the spawn help
	 * @param player The player requesting help
	 */
	private void handleSpawnHelp(Player player) {
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-USAGE"));
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-ONE-PARAM-USAGE"));
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-TWO-PARAM-USAGE"));
	}
	
	/**
	 * Handle the /spawn remove [worldname] command
	 * @param player The player executing the command
	 * @param worldname The world name to remove
	 */
	private void handleSpawnRemove(Player player, String worldname) {

		if (!Module.hasPermission(player, SpawnPermission.Remove.permission)) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-NO-PERMISSION").replace("%permission%", SpawnPermission.Remove.permission));
			return;
		}
		
		if (!m_spawn.containsKey(worldname)) {
			Module.sendMessage("bTransported", player.getPlayer(), m_module.getLanguageValue("COMMAND-SPAWN-WORLD-DOESNT-EXIST").replace("%worldname%", worldname));					
			return;
		}

		m_spawn.remove(worldname);
		deleteSpawnFromDatabase(worldname);

		Module.sendMessage("bTransported", player.getPlayer(), m_module.getLanguageValue("COMMAND-SPAWN-DELETED").replace("%worldname%", worldname));
	}
	
	/**
	 * Set the players current world spawn to the players location
	 * @param player The player using the command
	 */
	private void handleSpawnSet(Player player) {
		
		if (!Module.hasPermission(player, SpawnPermission.Set.permission)) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-NO-PERMISSION").replace("%permission%", SpawnPermission.Set.permission));
			return;
		}
		
		final Location location = player.getLocation();
		final String worldName = location.getWorld().getName();

		if (m_spawn.containsKey(worldName)) {
			m_spawn.remove(worldName);
			deleteSpawnFromDatabase(worldName);
		}

		m_spawn.put(worldName, location);
		addSpawnToDatabase(location);

		Module.sendMessage("bTransported", player.getPlayer(), m_module.getLanguageValue("COMMAND-SPAWN-ADDED").replace("%worldname%", worldName));
	}
	
	/**
	 * Show the spawn gui
	 * @param player The player to show it to
	 */
	private void handleSpawnGUI(Player player) {
		
		final int noofSpawns = this.m_spawn.size();
		final int ROW_COUNT = (int) Math.ceil(noofSpawns / 9.0f);

        GuiInventory inventory = new GuiInventory(bFundamentals.getInstance());
        inventory.createInventory("Spawn Selection", ROW_COUNT);

        for (Entry<String, Location> entry : m_spawn.entrySet()) {
			if (!Module.hasPermission(player, SpawnPermission.Spawn.permission + "." + entry.getKey())) {
				continue;
			}
			
			ItemStack item = new ItemStack(Material.COMPASS);
			String[] details = new String[1];
			details[0] = entry.getValue().getBlockX() + ", " + entry.getValue().getBlockY() + ", " + entry.getValue().getBlockZ();
			inventory.addMenuItem(entry.getKey(), item, details, new SpawnGuiCallback(m_module, player, entry));
		}
		
        inventory.open(player);
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
		
		db.query(query);
	}
	
	/**
	 * Delete a world spawn from the database
	 * @param worldName		The name of the world spawn to be deleted.
	 */
	private void deleteSpawnFromDatabase(final String worldName) {
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		
		String query = "DELETE FROM `perks_spawn` " +
				"WHERE world=" + "'" + worldName + "';";
		
		db.query(query, true);
	}
	
	/**
	 * Create the spawn database table should it not exist
	 */
	private void createDatabase() {
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		if (!db.tableExists("perks_spawn")) {			
			String query = "CREATE TABLE perks_spawn (" +
					"world VARCHAR(128)," +
					"x INT," +
					"y INT," +
					"z INT," +
					"yaw INT," +
					"pitch INT" +
					");";
			
			db.query(query, true);
		}
	}
	
	/**
	 * Load all the spawns out of the database into a hash map, saving a database lookup on command execution.
	 */
	private void loadSpawnsFromDatabase() {
		
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		
		String query = "SELECT * FROM perks_spawn";
		ResultSet result = db.queryResult(query);
		
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
			
			db.freeResult(result);
		}
	}

	@Override
	protected void onAnvilNameComplete(Player player, Location location, String name) {
		// unused
	}
	
	/**
     * Handle tab completion
     * @return A list of matches
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        List<String> matches = new ArrayList<String>();
        if (args.length == 0)
            return matches;
		
		if (args.length == 1) {
			// spawn <world name>
			// spawn <player_name> 
			// spawn set
			// spawn remove
			
			final String worldLookup = args[args.length - 1];
			
			if (("set").startsWith(worldLookup) || ("set").equalsIgnoreCase(worldLookup)) {
				matches.add("set");
			}
			
			if (("remove").startsWith(worldLookup) || ("remove").equalsIgnoreCase(worldLookup)) {
				matches.add("remove");
			}

			// world names
			for (Entry<String, Location> entry : m_spawn.entrySet()) {
				if (Module.hasPermission((Player)sender, SpawnPermission.Spawn.permission + "." + entry.getKey())) {
					if (entry.getKey().startsWith(worldLookup)) {
						matches.add(entry.getKey());
						continue;
					}
					
					if (entry.getKey().equalsIgnoreCase(worldLookup)) {
						matches.add(entry.getKey());
						continue;
					}
				}
			}
			
			// player names
			List<OfflinePlayer> players = m_module.matchPlayer(args[0], false);
			for (OfflinePlayer other : players) {
				matches.add(other.getName());
			}
		}
		else if (args.length == 2) {
			// spawn <player_name> <world_name>
			// spawn <world_name> 
			
			final String worldLookup = args[args.length - 1];
			if (("remove").startsWith(worldLookup) || ("remove").equalsIgnoreCase(worldLookup)) {
				matches.add("remove");
			}
			
			// world names
			for (Entry<String, Location> entry : m_spawn.entrySet()) {
				if (Module.hasPermission((Player)sender, SpawnPermission.Spawn.permission + "." + entry.getKey())) {
					if (entry.getKey().startsWith(worldLookup)) {
						matches.add(entry.getKey());
						continue;
					}
					
					if (entry.getKey().equalsIgnoreCase(worldLookup)) {
						matches.add(entry.getKey());
						continue;
					}
				}
			}
		}
		
		return matches;
	}
}
