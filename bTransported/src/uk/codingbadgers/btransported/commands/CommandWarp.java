package uk.codingbadgers.btransported.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import uk.codingbadgers.btransported.commands.callbacks.NewWarpGuiCallback;
import uk.codingbadgers.btransported.commands.callbacks.WarpGuiCallback;
import uk.codingbadgers.btransported.permissions.WarpPermission;
import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;

/**
 *
 * @author Sam
 */
public class CommandWarp extends CommandPlaceBase {

	/**
	 * The main module
	 */
	private bTransported m_module = null;
	
	/**
	 * Hashmap of warp locations
	 */
	private HashMap<String, Location> m_warp = new HashMap<String, Location>();
	
	/**
	 * Hash map of warp name and icon
	 */
	private HashMap<String, Material> m_warpIcon = new HashMap<String, Material>();

    /**
     *
     * @param module
     */
    public CommandWarp(bTransported module) {
		super(
			module, 
			"BF-WARP-ANVIL", 
			"warp", 
			"warp <name> | warp list | warp help | warp <name> <playername> | warp <name> all | warp <name> create | warp <name> remove | warp <name> seticon <material>"
		);
		m_module = module;
		
		createDatabase();
		loadWarpsFromDatabase();
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args)
	{
		if (!(sender instanceof Player)) {
			return true;
		}
		
		Player player = (Player)sender;
		
		if (!Module.hasPermission(player, WarpPermission.Warp.permission)) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", WarpPermission.Warp.permission));
			return true;
		}
		
		if (args.length == 0) {
			
			// Show warp GUI
			handleWarpGUI(player);
			return true;
		}
		else if (args.length == 1) {
			
			// Handle /warp list
			if (args[0].equalsIgnoreCase("list")) {
				
				handleWarpList(player);
				return true;
	
			// Handle /warp help
			} else if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
			
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-USAGE"));
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-ADD-USAGE"));
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-DELETE-USAGE"));
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-LIST-USAGE"));
				return true;
				
			// Handle /warp <name>
			} else {
				final String warpname = args[0];				
				warpPlayer(player, warpname);
				return true;
			}
			
		}
		else if (args.length == 2) {
			
			final String command = args[1];
			
			if (command.equalsIgnoreCase("create")) {
				// Handle /warp <name> create
				
				handleWarpCreate(player, args[0]);
				return true;
				
			} else if (command.equalsIgnoreCase("remove")) {
				// Handle /warp <name> remove
				
				handleWarpRemove(player, args[0]);
				return true;
				
			} else if (command.equalsIgnoreCase("all")) {
				// Handle /warp <name> all
				
				handleWarpAll(player, args[0]);
				return true;
				
			} else {
				// Handle /warp <name> <playername>
				handleWarpOtherPlayer(player, args[0], args[1]);
				return true;
			}
			
		}
		else if (args.length == 3) {
			
			final String command = args[1];
			
			// warp <name> seticon <material>
			if (command.equalsIgnoreCase("seticon")) {
				final String name = args[0];
				
				Material icon = null;
				try {
					icon = Material.valueOf(name);
				}
				catch(Exception ex) {
					icon = null;
				}
				
				if (icon == null) {
					icon = Material.getMaterial(name);
				}
				
				if (icon == null) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-SETICON-INVALID-MATERIAL"));
					return true;
				}
				
				if (!m_warp.containsKey(name)) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NOT-FOUND"));
					return true;
				}
				
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-ICON-SET"));
				m_warpIcon.put(name, icon);
				updateWarpIcon(name, icon);
				return true;
			}
		}
		
		// entered the wrong number of parameters...
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-USAGE"));
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-ADD-USAGE"));
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-DELETE-USAGE"));
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-LIST-USAGE"));
		
		return true;
	}
	
	/**
	 * 
	 * @param player
	 * @param warpname
	 * @param playername 
	 */
	private void handleWarpOtherPlayer(Player player, String warpname, String playername) {
		
		if (!Module.hasPermission(player, WarpPermission.WarpOther.permission)) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", WarpPermission.WarpOther.permission));
			return;
		}

		OfflinePlayer warpPlayer = Bukkit.getOfflinePlayer(playername);
		if (!warpPlayer.hasPlayedBefore()) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-OTHER-PLAYER-NOT-FOUND").replace("%playername%", playername));
			return;
		}

		final Location location = m_warp.get(warpname);
		m_module.teleportOfflinePlayer(warpPlayer, location);
		
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-COMPLETE").replace("%warpname%", warpname));
		if (warpPlayer.isOnline()) {
			Module.sendMessage("bTransported", warpPlayer.getPlayer(), m_module.getLanguageValue("COMMAND-WARP-COMPLETE").replace("%warpname%", warpname));
		}
	}
	
	/**
	 * Handle the /warp <name> all command
	 * @param player The player who executed the command
	 * @param warpname The warp name to warp too
	 */
	private void handleWarpAll(Player player, String warpname) {
		
		if (!Module.hasPermission(player, WarpPermission.All.permission)) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", WarpPermission.All.permission));
			return;
		}

		for (Player warpPlayer : Bukkit.getOnlinePlayers()) {
			if (warpPlayer.getName().equalsIgnoreCase(player.getName())) {
				continue;
			}
			
			warpPlayer(warpPlayer, warpname);
		}
	}
	
	/**
	 * Handle the /warp <name> remove command
	 * @param player The player who executed the command
	 * @param warpname The warp name to remove
	 */
	private void handleWarpRemove(Player player, String warpname) {
		
		if (!Module.hasPermission(player, WarpPermission.Remove.permission)) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", WarpPermission.Remove.permission));
			return;
		}
		
		if (!m_warp.containsKey(warpname)) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NOT-FOUND"));
			return;
		}				

		m_warp.remove(warpname);
		deleteWarpFromDatabase(warpname);
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-DELETED").replace("%warpname%", warpname));
	}
	
	/**
	 * Handle the /warp <name> create command
	 * @param player The player who entered the command
	 * @param warpname The name of the warp
	 */
	private void handleWarpCreate(Player player, String warpname) {
		
		if (!Module.hasPermission(player, WarpPermission.Create.permission)) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", WarpPermission.Create.permission));
			return;
		}
		
		warpname = formatName(warpname);

		if (warpname.equalsIgnoreCase("list") || warpname.equalsIgnoreCase("help") || warpname.equalsIgnoreCase("?")) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NAME-RESERVED"));
			return;
		}

		if (m_warp.containsKey(warpname)) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NAME-USED"));
			return;
		}

		final Location location = player.getLocation();
		m_warp.put(warpname, location);
		addWarpToDatabase(warpname, location, Material.EYE_OF_ENDER);
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-CREATED").replace("%warpname%", warpname));
	}
	
	/**
	 * Handle the /warp list command
	 * @param player The player who entered the command
	 */
	private void handleWarpList(Player player) {
		
		if (!Module.hasPermission(player, WarpPermission.List.permission)) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", WarpPermission.List.permission));
			return;
		}

		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-LIST"));
		for (Entry<String, Location> entry : m_warp.entrySet()) {
			final String name = entry.getKey();
			final Location location = entry.getValue();				
			if (Module.hasPermission(player, WarpPermission.Warp.permission + "." + name)) {
				Module.sendMessage("bTransported", player, " - " + ChatColor.GOLD + name + ChatColor.WHITE + " at " + (int)location.getX() + ", " + (int)location.getY() + ", " + (int)location.getZ() + " in " + location.getWorld().getName());
			}
		}
		
	}
	
	/**
	 * Show the warp inventory gui to a player
	 * @param player The player to show the GUI
	 */
	private void handleWarpGUI(Player player) {
		
		if (!Module.hasPermission(player, WarpPermission.Gui.permission)) {
			Module.sendMessage("Warp", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION"));
			return;
		}

		final int noofWarps = m_warp.size();
		int ROW_COUNT = (int) Math.ceil(noofWarps / 9.0f);
		ROW_COUNT = ROW_COUNT == 0 ? 1 : ROW_COUNT;
		
		final int LAST_SLOT = (ROW_COUNT * 9) - 1;
		
        GuiInventory inventory = new GuiInventory(bFundamentals.getInstance());
        inventory.createInventory("Warp Selection", ROW_COUNT);

        // Show the warps
        for (Entry<String, Location> warp : m_warp.entrySet()) {
			if (!Module.hasPermission(player, WarpPermission.Warp.permission + "." + warp.getKey())) {
				continue;
			}
			
			ItemStack item = new ItemStack(Material.EYE_OF_ENDER);
			if (m_warpIcon.containsKey(warp.getKey())) {
				item = new ItemStack(m_warpIcon.get(warp.getKey()));
			}
			
			String[] details = new String[2];
			details[0] = warp.getValue().getBlockX() + ", " + warp.getValue().getBlockY() + ", " + warp.getValue().getBlockZ();
			details[1] = warp.getValue().getWorld().getName();
			inventory.addMenuItem(warp.getKey(), item, details, new WarpGuiCallback(player, warp.getKey(), this));
		}
		
		if (Module.hasPermission(player, WarpPermission.Create.permission)) {
			// Show the create warp item
			Location playerLocation = player.getLocation();

			// Add the writen book home creator
			ItemStack item = new ItemStack(Material.PORTAL);
			String[] details = new String[3];
			details[0] = "Create a new warp at";
			details[1] = playerLocation.getBlockX() + ", " + playerLocation.getBlockY() + ", " + playerLocation.getBlockZ();
			details[2] = "in " + playerLocation.getWorld().getName();

			inventory.addMenuItem("New Warp", item, details, LAST_SLOT, new NewWarpGuiCallback(player, this));
		}
		
        inventory.open(player);
		
	}
	
	/**
	 * 
	 * @param player
	 * @param warpName
	 * @return 
	 */
	public boolean warpPlayer(final Player player, String warpName) {
		
		if (!Module.hasPermission(player, WarpPermission.Warp.permission + "." + warpName)) {
			if (!warpName.equalsIgnoreCase(player.getName())) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", "perks.btransported.warp." + warpName));
				return true;
			}
		}
		
		if (!m_warp.containsKey(warpName)) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NOT-FOUND").replace("%warpname%", warpName));					
			return false;
		}
		
		final Location location = m_warp.get(warpName);
		player.teleport(location);				
		
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-COMPLETE").replace("%warpname%", warpName));
				
		return true;
	}
	
	private void addWarpToDatabase(final String warpName, final Location location, final Material icon) {
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		
		String query = "INSERT INTO `perks_warps` " +
				"(`name`,`world`,`x`,`y`,`z`,`yaw`,`pitch`, `icon`) VALUES (" + 
				"'" + warpName + "'," +
				"'" + location.getWorld().getName() + "'," +
				"'" + location.getX() + "'," +
				"'" + location.getY() + "'," +
				"'" + location.getZ() + "'," +
				"'" + location.getYaw() + "'," +
				"'" + location.getPitch() + "'," +
				"'" + icon.name() +
				"');";
		
		db.query(query);
	}
	
	private void updateWarpIcon(final String warpName, final Material icon) {
	
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		
		String query = "UPDATE `perks_warps` " +
				"SET icon='" + icon.name() + "' " +
				"WHERE name='" + warpName +
				"');";
		
		db.query(query);		
	}
	
	private void deleteWarpFromDatabase(final String warpName) {
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		
		String query = "DELETE FROM `perks_warps` " +
				"WHERE name=" + "'" + warpName + "';";
		
		db.query(query, true);
	}
	
	private void createDatabase() {
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		if (!db.tableExists("perks_warps")) {			
			String query = "CREATE TABLE perks_warps (" +
					"name VARCHAR(64)," +
					"world VARCHAR(128)," +
					"x INT," +
					"y INT," +
					"z INT," +
					"yaw INT," +
					"pitch INT," +
					"icon VARCHAR(64)" +
					");";
			
			db.query(query, true);
		}
	}
	
	private void loadWarpsFromDatabase() {
		
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		
		String query = "SELECT * FROM perks_warps";
		ResultSet result = db.queryResult(query);
		
		if (result != null) {
			try {
				// while we have another result, read in the data
				while (result.next()) {
		            String worldName = result.getString("world");
		            String warpName = result.getString("name");
	
		            int x = result.getInt("x");
		            int y = result.getInt("y");
		            int z = result.getInt("z");
		            int pitch = result.getInt("pitch");
		            int yaw = result.getInt("yaw");
					Material icon = Material.valueOf(result.getString("icon"));
		            
		            World world = Bukkit.getServer().getWorld(worldName);
		            Location location = new Location(world, x, y, z, yaw, pitch);
		                        
		            m_warp.put(warpName, location);		
					m_warpIcon.put(warpName, icon);
		        }
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}
			
			db.freeResult(result);
		}
	}
	
	/**
	 * Handle tab completion
     * @return 
	 */
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

		List<String> matches = new ArrayList<String>();
        if (args.length == 0)
            return matches;
		
		if (args.length == 1) {
			// warp <name>
			// warp list
			// warp help 
			final String warpLookup = args[args.length - 1].toLowerCase();
			if (("list").startsWith(warpLookup) || ("list").equalsIgnoreCase(warpLookup)) {
				matches.add("list");
			}
			
			if (("help").startsWith(warpLookup) || ("help").equalsIgnoreCase(warpLookup)) {
				matches.add("help");
			}
			
			// warp names
			for (Entry<String, Location> warp : m_warp.entrySet()) {
				if (Module.hasPermission((Player)sender, WarpPermission.Warp.permission + "." + warp.getKey())) {
					if (warp.getKey().toLowerCase().startsWith(warpLookup)) {
						matches.add(warp.getKey());
						continue;
					}
					
					if (warp.getKey().toLowerCase().equalsIgnoreCase(warpLookup)) {
						matches.add(warp.getKey());
						continue;
					}
				}
			}
		}
		else if (args.length == 2) {
			// warp <name> <playername>
			// warp <name> all
			// warp <name> create
			// warp <name> remove 
			final String playerLookup = args[args.length - 1];
			if (("all").startsWith(playerLookup) || ("all").equalsIgnoreCase(playerLookup)) {
				matches.add("all");
			}
			
			if (("create").startsWith(playerLookup) || ("create").equalsIgnoreCase(playerLookup)) {
				matches.add("create");
			}
			
			if (("remove").startsWith(playerLookup) || ("remove").equalsIgnoreCase(playerLookup)) {
				matches.add("remove");
			}
			
			if (("seticon").startsWith(playerLookup) || ("seticon").equalsIgnoreCase(playerLookup)) {
				matches.add("seticon");
			}
			
			// player names
			List<OfflinePlayer> players = m_module.matchPlayer(playerLookup, false);
			for (OfflinePlayer other : players) {
				matches.add(other.getName());
			}
		}
		else if (args.length == 3) {
			// warp <name> seticon <material>
			final String materialname = args[args.length - 1];
			
			for (Material material : Material.values()) {
				if (material.toString().startsWith(materialname)) {
					matches.add(material.toString());
					continue;
				}

				if (material.toString().equalsIgnoreCase(materialname)) {
					matches.add(material.toString());
					continue;
				}
			}
		}
		
		return matches;
	}

	@Override
	protected void onAnvilNameComplete(Player player, Location location, String name) {
		handleWarpCreate(player, name);
	}
}
