package uk.codingbadgers.btransported.commands;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.gui.GuiInventory;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.btransported.bTransported;
import uk.codingbadgers.btransported.commands.callbacks.HomeGuiCallback;
import uk.codingbadgers.btransported.commands.callbacks.NewHomeGuiCallback;
import uk.codingbadgers.btransported.commands.home.PlayerHome;
import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;

/**
 * @author N3wton
 */
public class CommandHome extends CommandPlaceBase {

    /**
     * Map of player name to list of homes
     */
    private final Map<String, List<PlayerHome>> m_homes;
    
    /**
     * The name of the anvil inventory used for naming a home.
     */
    public static final String ANVIL_INVENTORY_NAME = "BF-HOME-ANVIL";
	
	/**
	 * A map of rank name and max number of homes that rank can have
	 */
	private Map<String, Integer> m_maxHomeCount;

    /**
     * Class constructor
     *
     * @param module	The bFundamentals module
     */
    public CommandHome(bTransported module) {
        super(
			module, 
			ANVIL_INVENTORY_NAME, 
			"home", 
			"home | home <home name> | home <player name> <home name> | home <name> set | home <home name> remove | home <player name> <home name> remove"
		);
		m_homes = new HashMap<String, List<PlayerHome>>();
		m_maxHomeCount = new HashMap<String, Integer>();
		
		loadConfig();
		createDatabase();
		loadHomesFromDatabase();
    }
	
	/**
	 * Create the homes database
	 */
	private void createDatabase() {
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		if (!db.tableExists("perks_homes")) {			
			String query = "CREATE TABLE perks_homes (" +
					"hash BIGINT," +																			
					"name VARCHAR(64)," +
					"owner VARCHAR(64)," +
					"world VARCHAR(128)," +
					"x FLOAT," +
					"y FLOAT," +
					"z FLOAT," +
					"yaw FLOAT," +
					"pitch FLOAT" +
					");";
			
			db.query(query, true);
		}
	}
		
	/**
	 * Add a home to the homes database
	 * @param home The home to add
	 */
	private void addHomeToDatabase(final PlayerHome home) {
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		
		String query = "INSERT INTO `perks_homes` " +
				"(`hash`,`name`,`owner`,`world`,`x`,`y`,`z`,`yaw`,`pitch`) VALUES (" + 
				"'" + home.getHash() + "'," +
				"'" + home.getName() + "'," +
				"'" + home.getOwnerName() + "'," +
				"'" + home.getLocation().getWorld().getName() + "'," +
				"'" + home.getLocation().getX() + "'," +
				"'" + home.getLocation().getY() + "'," +
				"'" + home.getLocation().getZ() + "'," +
				"'" + home.getLocation().getYaw() + "'," +
				"'" + home.getLocation().getPitch() + 
				"');";
		
		db.query(query);
	}
	
	/**
	 * Remove a home from the database
	 * @param home The home to remove 
	 */
	private void removeHomeFromDatabase(final PlayerHome home) {
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		
		String query = "DELETE FROM `perks_homes` " +
				"WHERE hash=" + "'" + home.getHash() + "';";
		
		db.query(query, true);
	}
	
	/**
	 * Load all homes from the database
	 */
	private void loadHomesFromDatabase() {
		
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		
		String query = "SELECT * FROM perks_homes";
		ResultSet result = db.queryResult(query);
		
		if (result != null) {
			try {
				// while we have another result, read in the data
				while (result.next()) {
		            String worldName = result.getString("world");
		            String homeName = result.getString("name");
					String ownerName = result.getString("owner");
					
					int hash = result.getInt("hash");
	
		            float x = result.getFloat("x");
		            float y = result.getFloat("y");
		            float z = result.getFloat("z");
		            float pitch = result.getFloat("pitch");
		            float yaw = result.getFloat("yaw");
		            
		            World world = Bukkit.getServer().getWorld(worldName);
		            Location location = new Location(world, x, y, z, yaw, pitch);
					
					List<PlayerHome> homes;
					if (m_homes.containsKey(ownerName)) {
						homes = m_homes.get(ownerName);
					} else {
						homes = new ArrayList<PlayerHome>();
					}
					
					PlayerHome home = new PlayerHome(homeName, ownerName, location, hash);
					homes.add(home);
					
					m_homes.put(ownerName, homes);
		        }
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}
			
			db.freeResult(result);
		}
		
	}
	
	/**
	 * Load the homes config
	 */
	private void loadConfig() {
		
		File file = new File(m_module.getDataFolder() + File.separator + "home.yml");
		if (!file.exists()) {
			// Create a new home config
			try {
				file.createNewFile();
			}
			catch (Exception ex) {
				bFundamentals.log(Level.SEVERE, "Failed to create new home config!", ex);
				return;
			}
			
			// Set the default config options
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			config.set("maxhomes.default", 1);
			
			String[] groups = bFundamentals.getPermissions().getGroups();
			for (String group : groups) {
				config.set("maxhomes." + group, 1);
			}
			
			// Save the new config
			try {
				config.save(file);
			} catch (Exception ex) {
				bFundamentals.log(Level.SEVERE, "Failed to save new home config!", ex);
				return;
			}
		}
		
		// Load the config
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		final int MAX_HOMES = 53;		
		int maxHomes = config.getInt("maxhomes.default");
		m_maxHomeCount.put("default", maxHomes <= MAX_HOMES ? maxHomes : MAX_HOMES);
		
		String[] groups = bFundamentals.getPermissions().getGroups();
		for (String group : groups) {
			if (config.contains("maxhomes." + group)) {
				maxHomes = config.getInt("maxhomes." + group);
				m_maxHomeCount.put(group, maxHomes <= MAX_HOMES ? maxHomes : MAX_HOMES);
			}
		}
	}

    @Override
    /**
     * Called when a command is executed. Returning true indicates the command
     * was handled.
     *
     * @param sender	The thing that executed the command, player or console.
     * @param label The label of the command
     * @param args	The arguments passed with command
     */
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        // /Home cannot be used via the console currently.
        if (!(sender instanceof Player)) {
            return true;
        }

        // Cast the sender to a player
        final Player player = (Player) sender;

        // Make sure the sending player has the spawn permission
        if (!Module.hasPermission(player, "perks.btransported.home")) {
            Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-HOME-NO-PERMISSION").replace("%permission%", "perks.btransported.home"));
            return true;
        }

        if (args.length == 0) {

            // handle /home			
            handleHomeGUI(player);
            return true;
        }
        else if (args.length == 1) {
            
            // handle /home <home name>
            handleHomeName(player, args[0]);
            return true;
            
        }
        else if (args.length == 2) {
            
			final String command = args[1];
			
			// handle /home <home name> set
            if (command.equalsIgnoreCase("set")) {
				
				if (!Module.hasPermission(player, "btransported.home")) {
					Module.sendMessage("Home", player, m_module.getLanguageValue("COMMAND-HOME-NO-PERMISSION"));
					return true;
				}
				
				if (getHomeCount(player) >= getMaxHomes(player)) {
					Module.sendMessage("Home", player, m_module.getLanguageValue("COMMAND-HOME-REACHED-MAX"));
					return true;
				}
				
                addNewHome(player, player.getLocation(), args[0]);
                return true;
            }
			// handle /home <home name> remove
            else if (command.equalsIgnoreCase("remove")) {
                handleHomeRemove(player, player.getName(), args[0]);
                return true;
            }
            // handle /home <player name> <home name>
			else if (handleHomePlayerName(player, args[0], args[1])) {
                return true;
            }            
            
        }
        else if (args.length == 3) {
            
			// handle /home <player name> <home name> remove
            final String command = args[2];
            if (command.equalsIgnoreCase("remove")) {
                handleHomeRemove(player, args[0], args[1]);
                return true;
            }
        }

        return false;
    }
    
	/**
	 * Handle the remove home command
	 * @param sender The player who is executing the command
	 * @param playerName The owner of the home
	 * @param homeName The name of the home
	 * @return True on success, false otherwise
	 */
    public boolean handleHomeRemove(Player sender, String playerName, String homeName) {
		
		// If the sender is not the owner of the home check they have perms
		if (!sender.getName().equalsIgnoreCase(playerName)) {
			if (!Module.hasPermission(sender, "btransported.home.other.remove")) {
				Module.sendMessage("Home", sender, m_module.getLanguageValue("COMMAND-HOME-NO-PERMISSION"));
				return true;
			}
		}
        
        PlayerHome home = getHomeFromName(playerName, homeName);
        if (home == null) {
			Module.sendMessage("Home", sender, m_module.getLanguageValue("COMMAND-HOME-NOT-FOUND"));
            return false;
        }
        
        List<PlayerHome> homes = m_homes.get(playerName);
        homes.remove(home);
        m_homes.put(playerName, homes);
		
		removeHomeFromDatabase(home);
		
		Module.sendMessage("Home", sender, m_module.getLanguageValue("COMMAND-HOME-REMOVED"));
        return true;        
    }
    
	/**
	 * Handle the command /home <player name> <home name>
	 * @param player
	 * @param playerName
	 * @param homeName
	 * @return 
	 */
    private boolean handleHomePlayerName(Player player, String playerName, String homeName) {
        
		// If the sender is not the owner of the home check they have perms
		if (!player.getName().equalsIgnoreCase(playerName)) {
			if (!Module.hasPermission(player, "btransported.home.other")) {
				Module.sendMessage("Home", player, m_module.getLanguageValue("COMMAND-HOME-NO-PERMISSION"));
				return true;
			}
		}
		
        OfflinePlayer homePlayer = Bukkit.getOfflinePlayer(playerName);
        if (!homePlayer.hasPlayedBefore()) {
			Module.sendMessage("Home", player, m_module.getLanguageValue("COMMAND-HOME-COULD-NOT-FIND-PLAYER"));
            return false;
        }
        
        PlayerHome home = getHomeFromName(playerName, homeName);
        if (home == null) {
			Module.sendMessage("Home", player, m_module.getLanguageValue("COMMAND-HOME-COULD-NOT-FIND-HOME"));
            return false;
        }

        return m_module.teleportOfflinePlayer(player, home.getLocation());
    }
    
	/**
	 * Handle the command /home <home name>
	 * @param player
	 * @param homeName
	 * @return 
	 */
    private boolean handleHomeName(Player player, String homeName) {
        
        PlayerHome home = getHomeFromName(player.getName(), homeName);
        if (home == null) {
			Module.sendMessage("Home", player, m_module.getLanguageValue("COMMAND-HOME-COULD-NOT-FIND-HOME"));
            return false;
        }

        return m_module.teleportOfflinePlayer(player, home.getLocation());
    }

	/**
	 * Get a player home from a specified name and player
	 * @param playername
	 * @param homeName
	 * @return 
	 */
    private PlayerHome getHomeFromName(String playername, String homeName) {
        
        if (!m_homes.containsKey(playername)) {
            return null;
        }
        
        List<PlayerHome> homes = m_homes.get(playername);
        for (PlayerHome home : homes) {
            if (home.getName().equalsIgnoreCase(homeName)) {
                return home;
            }
        }
        
        return null;
    }
    
	/**
	 * Open up the home gui
	 * @param player The player to show it too
	 */
    public void handleHomeGUI(Player player) {
		
		if (!Module.hasPermission(player, "btransported.home")) {
			Module.sendMessage("Home", player, m_module.getLanguageValue("COMMAND-HOME-NO-PERMISSION"));
			return;
		}

		final int maxHomes = this.getMaxHomes(player);
		final int ROW_COUNT = (int) Math.ceil(maxHomes / 9.0f);
		final int LAST_SLOT = (ROW_COUNT * 9) - 1;
		
        GuiInventory inventory = new GuiInventory(bFundamentals.getInstance());
        inventory.createInventory("Home Selection (" + getHomeCount(player) + "/" + maxHomes + ")", ROW_COUNT);

        // If they already have homes, list them
        if (m_homes.containsKey(player.getName())) {
            List<PlayerHome> homes = m_homes.get(player.getName());

            for (PlayerHome home : homes) {
                ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
                String[] details = new String[2];
                details[0] = home.getLocation().getBlockX() + ", " + home.getLocation().getBlockY() + ", " + home.getLocation().getBlockZ();
                details[1] = home.getLocation().getWorld().getName();
                inventory.addMenuItem(home.getName(), item, details, new HomeGuiCallback(m_module, player, home, this));
            }
        }
		
		if (getHomeCount(player) >= getMaxHomes(player)) {
			// max home count reached
			ItemStack item = new ItemStack(Material.BOOKSHELF);
			String[] details = new String[4];
			details[0] = "You have reached the maximum";
			details[1] = "number of homes possible for your rank.";
			details[2] = "Remove other homes or rank up to";
			details[3] = "add more homes.";
			
			inventory.addMenuItem("Max Homes Reached", item, details, LAST_SLOT, null);
		}
		else {
			Location playerLocation = player.getLocation();

			// Add the writen book home creator
			ItemStack item = new ItemStack(Material.BOOK_AND_QUILL);
			String[] details = new String[3];
			details[0] = "Create a new home at";
			details[1] = playerLocation.getBlockX() + ", " + playerLocation.getBlockY() + ", " + playerLocation.getBlockZ();
			details[2] = "in " + playerLocation.getWorld().getName();

			inventory.addMenuItem("New Home", item, details, LAST_SLOT, new NewHomeGuiCallback(m_module, player, this));
		}
		
        inventory.open(player);
    }

    /**
     * Format a home name into a non spaced camel case name
     * @param name The name to format
     * @return The formated version of the name
     */
    private String formatHomeName(String name) {
        
        name = name.toLowerCase();
        String[] nameParts = name.split(" ");
        String formattedName = "";
        for (String part : nameParts) {
            String camelPart = part.substring(0, 1).toUpperCase();
            if (part.length() > 1) {
                camelPart += part.substring(1);
            }
            formattedName += camelPart;
        }      
        
        return formattedName;
    }
    
    /**
     *
     * @param player
     * @param location
     * @param name
     */
    public void addNewHome(Player player, Location location, String name) {
        
        List<PlayerHome> homes;
        if (m_homes.containsKey(player.getName())) {
            homes = m_homes.get(player.getName());
        } else {
            homes = new ArrayList<PlayerHome>();
        }

        if (homes.size() >= this.getMaxHomes(player)) {
            Module.sendMessage("Home", player, m_module.getLanguageValue("COMMAND-HOME-REACHED-MAX"));
            return;
        }

        PlayerHome home = new PlayerHome(formatHomeName(name), player.getName(), location);
        homes.add(home);

        m_homes.put(player.getName(), homes);
		addHomeToDatabase(home);
        
        Module.sendMessage("Home", player, m_module.getLanguageValue("COMMAND-HOME-ADDED-NEW"));
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

        String playerName = sender.getName();
                
        //home <home name>
		//home <home name> set|remove
        //home <player name> <home name>
        if (args.length == 1 || args.length == 2) {
            
			if (args.length == 2) {
                playerName = args[0];
				matches.add("set");
				matches.add("remove");
            }
			else if (args.length == 1) {
				// first param can be home name or player name
				List<OfflinePlayer> players = m_module.matchPlayer(args[0], false);
				for (OfflinePlayer other : players) {
					matches.add(other.getName());
				}
			}
            
            if (!m_homes.containsKey(playerName)) {
                return matches;
            }
            
            List<PlayerHome> homes = m_homes.get(playerName);
			
			final String homeLookup = args[args.length - 1];
			
            for (PlayerHome home : homes) {
				if (home.getName().equalsIgnoreCase(homeLookup)) {
					matches.add(home.getName());
					continue;
				}
				
				if (home.getName().toLowerCase().startsWith(homeLookup)) {
					matches.add(home.getName());
				}
            }            
        }
		else if (args.length == 3) {
			// home <player name> <home name> remove
			matches.add("remove");
		}

        return matches;
    }
	
	/**
	 * Get the maximum number of homes a player can have
	 * @param player The player to get for
	 * @return The number of homes
	 */
	private int getMaxHomes(Player player) {
		
		int maxHomes = m_maxHomeCount.get("default");
		String group = bFundamentals.getPermissions().getPrimaryGroup((String)null, player.getName());
		if (m_maxHomeCount.containsKey(group)) {
			maxHomes = m_maxHomeCount.get(group);
		}
		
		return maxHomes;
	}

	/**
	 * Get the number of homes a player has
	 * @param player The player to count the homes of
	 * @return The number of homes the player has
	 */
	private int getHomeCount(Player player) {
		
		if (!m_homes.containsKey(player.getName())) {
			return 0;
		}
		
		return m_homes.get(player.getName()).size();
	}
	
	@Override
	/**
	 * Called when an anvil naming gui is completed
	 */
	protected void onAnvilNameComplete(Player player, Location location, String name) {
		addNewHome(player, location, name);
	}
}
