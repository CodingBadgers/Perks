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
import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;

/**
 *
 * @author Sam
 */
public class CommandWarp extends CommandPlaceBase {

	private bTransported m_module = null;
	
	private HashMap<String, Location> m_warp = new HashMap<String, Location>();

    /**
     *
     * @param module
     */
    public CommandWarp(bTransported module) {
		super(
			module, 
			"BF-WARP-ANVIL", 
			"warp", 
			"warp <name> | warp list | warp help | warp <name> <playername> | warp <name> all | warp <name> create | warp <name> remove"
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
		
		if (!Module.hasPermission(player, "perks.btransported.warp")) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", "perks.btransported.warp"));
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
		
		if (!Module.hasPermission(player, "perks.btransported.warp.other")) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", "perks.btransported.warp.other"));
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
		
		if (!Module.hasPermission(player, "perks.btransported.warp.all")) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", "perks.btransported.warp.all"));
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
		
		if (!Module.hasPermission(player, "perks.btransported.warp.remove")) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", "perks.btransported.warp.remove"));
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
		
		if (!Module.hasPermission(player, "perks.btransported.warp.create")) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", "perks.btransported.warp.create"));
			return;
		}

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
		addWarpToDatabase(warpname, location);
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-CREATED").replace("%warpname%", warpname));
	}
	
	/**
	 * Handle the /warp list command
	 * @param player The player who entered the command
	 */
	private void handleWarpList(Player player) {
		
		if (!Module.hasPermission(player, "perks.btransported.warp.list")) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", "perks.btransported.warp.list"));
			return;
		}

		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-LIST"));
		for (Entry<String, Location> entry : m_warp.entrySet()) {
			final String name = entry.getKey();
			final Location location = entry.getValue();				
			if (Module.hasPermission(player, "perks.btransported.warp." + name)) {
				Module.sendMessage("bTransported", player, " - " + ChatColor.GOLD + name + ChatColor.WHITE + " at " + (int)location.getX() + ", " + (int)location.getY() + ", " + (int)location.getZ() + " in " + location.getWorld().getName());
			}
		}
		
	}
	
	/**
	 * Show the warp inventory gui to a player
	 * @param player The player to show the GUI
	 */
	private void handleWarpGUI(Player player) {
		
		if (!Module.hasPermission(player, "btransported.warp")) {
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
			ItemStack item = new ItemStack(Material.EYE_OF_ENDER);
			String[] details = new String[2];
			details[0] = warp.getValue().getBlockX() + ", " + warp.getValue().getBlockY() + ", " + warp.getValue().getBlockZ();
			details[1] = warp.getValue().getWorld().getName();
			inventory.addMenuItem(warp.getKey(), item, details, null);
		}
		
		if (Module.hasPermission(player, "btransported.warp.set")) {
			// Show the create warp item
			Location playerLocation = player.getLocation();

			// Add the writen book home creator
			ItemStack item = new ItemStack(Material.PORTAL);
			String[] details = new String[3];
			details[0] = "Create a new warp at";
			details[1] = playerLocation.getBlockX() + ", " + playerLocation.getBlockY() + ", " + playerLocation.getBlockZ();
			details[2] = "in " + playerLocation.getWorld().getName();

			inventory.addMenuItem("New Warp", item, details, LAST_SLOT, null);
		}
		
        inventory.open(player);
		
	}
	
	private boolean warpPlayer(final Player player, String warpName) {
		
		warpName = warpName.toLowerCase();
		
		if (!Module.hasPermission(player, "perks.btransported.warp." + warpName)) {
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
	
	private void addWarpToDatabase(final String warpName, final Location location) {
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		
		String query = "INSERT INTO `perks_warps` " +
				"(`name`,`world`,`x`,`y`,`z`,`yaw`,`pitch`) VALUES (" + 
				"'" + warpName + "'," +
				"'" + location.getWorld().getName() + "'," +
				"'" + location.getX() + "'," +
				"'" + location.getY() + "'," +
				"'" + location.getZ() + "'," +
				"'" + location.getYaw() + "'," +
				"'" + location.getPitch() + 
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
					"pitch INT" +
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
		            
		            World world = Bukkit.getServer().getWorld(worldName);
		            Location location = new Location(world, x, y, z, yaw, pitch);
		                        
		            m_warp.put(warpName, location);		            
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
		
		Player player = null;
		if (sender instanceof Player) {
			player = (Player)sender;
		}
		
		List<String> matches = new ArrayList<String>();
		if (args.length == 0) {
			for (String warpName : m_warp.keySet()) {
				if (player == null || Module.hasPermission(player, "perks.btransported.warp." + warpName)) {
					matches.add(warpName);
				}
			}
			return matches;
		}
		
		String name = args[args.length - 1];
		for (String warpName : m_warp.keySet()) {
			if (warpName.toLowerCase().startsWith(name.toLowerCase())) {
				if (player == null || Module.hasPermission(player, "perks.btransported.warp." + warpName)) {
					matches.add(warpName);
				}
			}
		}
		
		return matches;
		
	}

	@Override
	protected void onAnvilNameComplete(Player player, Location location, String name) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
