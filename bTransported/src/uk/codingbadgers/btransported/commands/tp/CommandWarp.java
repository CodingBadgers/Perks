package uk.codingbadgers.btransported.commands.tp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.btransported.bTransported;
import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;

public class CommandWarp extends ModuleCommand {

	private bTransported m_module = null;
	
	private HashMap<String, Location> m_warp = new HashMap<String, Location>();
	
	public CommandWarp(bTransported module) {
		super("warp", "warp <name> | warp <playername> <name> | warp all <name> | warp list | warp add <name> | warp delete <name>");
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
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-USAGE"));
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-ADD-USAGE"));
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-DELETE-USAGE"));
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-LIST-USAGE"));
			return true;
		}
		
		// Handle /warp list and /warp <name>
		if (args.length == 1) {
			
			if (args[0].equalsIgnoreCase("list")) {
				// Handle /warp list
				
				if (!Module.hasPermission(player, "perks.btransported.warp.list")) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", "perks.btransported.warp.list"));
					return true;
				}
				
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-LIST"));
				for (Entry<String, Location> entry : m_warp.entrySet()) {
					final String name = entry.getKey();
					final Location location = entry.getValue();				
					if (Module.hasPermission(player, "perks.btransported.warp." + name)) {
						Module.sendMessage("bTransported", player, " - " + name + " at " + (int)location.getX() + ", " + (int)location.getY() + ", " + (int)location.getZ() + " in " + location.getWorld().getName());
					}
				}
				
				return true;
				
			} else {
				// Handle /warp <name>
				final String warpname = args[0];				
				warpPlayer(player, warpname);
				return true;
			}
			
		}
		
		// Handle /warp <playername> <name>, warp all <name>, warp add <name>, warp delete <name>
		if (args.length == 2) {
			
			final String command = args[0];
			
			if (command.equalsIgnoreCase("add")) {
				// Handle /warp add <name>
				
				if (!Module.hasPermission(player, "perks.btransported.warp.add")) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", "perks.btransported.warp.add"));
					return true;
				}
				
				final String warpname = args[1];
				
				if (warpname.equalsIgnoreCase("add") || warpname.equalsIgnoreCase("delete") || warpname.equalsIgnoreCase("all") || warpname.equalsIgnoreCase("list")) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NAME-RESERVED"));
					return true;
				}
				
				if (m_warp.containsKey(warpname)) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NAME-USED"));
					return true;
				}
				
				final Location location = player.getLocation();
				m_warp.put(warpname, location);
				addWarpToDatabase(warpname, location);
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-CREATED").replace("%warpname%", warpname));
				
			} else if (command.equalsIgnoreCase("delete")) {
				// Handle /warp delete <name>
				
				if (!Module.hasPermission(player, "perks.btransported.warp.delete")) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", "perks.btransported.warp.delete"));
					return true;
				}
				
				final String warpname = args[1];
				
				if (m_warp.containsKey(warpname)) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NOT-FOUND"));
					return true;
				}				
				
				m_warp.remove(warpname);
				deleteWarpToDatabase(warpname);
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-DELETED").replace("%warpname%", warpname));
				
				return true;
				
			} else if (command.equalsIgnoreCase("all")) {
				// Handle /warp all <name>
				
				if (!Module.hasPermission(player, "perks.btransported.warp.all")) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", "perks.btransported.warp.all"));
					return true;
				}
				
				final String warpname = args[1];
					
				for (Player warpPlayer : Bukkit.getServer().getOnlinePlayers()) {
					if (!warpPlayer(warpPlayer, warpname)) {
						return true;
					}
				}
				
				return true;
				
			} else {
				// Handle /warp <playername> <name>
				
				if (!Module.hasPermission(player, "perks.btransported.warp.other")) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", "perks.btransported.warp.other"));
					return true;
				}
				
				final String playerName = command;
				final String warpname = args[1];
				
				Player warpPlayer = Bukkit.getPlayer(playerName);
				if (warpPlayer == null) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-OTHER-PLAYER-NOT-FOUND").replace("%playername%", playerName));
					return true;
				}
				
				warpPlayer(warpPlayer, warpname);
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
	
	private boolean warpPlayer(final Player player, final String warpName) {
		
		if (!Module.hasPermission(player, "perks.btransported.warp." + warpName)) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", "perks.btransported.warp." + warpName));
			return true;
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
		
		db.Query(query);
	}
	
	private void deleteWarpToDatabase(final String warpName) {
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		
		String query = "DELETE FROM `perks_warps` " +
				"WHERE player=" + "'" + warpName + "';";
		
		db.Query(query, true);
	}
	
	private void createDatabase() {
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		if (!db.TableExists("perks_warps")) {			
			String query = "CREATE TABLE perks_warps (" +
					"name VARCHAR(64)," +
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
	
	private void loadWarpsFromDatabase() {
		
		BukkitDatabase db = bFundamentals.getBukkitDatabase();
		
		String query = "SELECT * FROM perks_warps";
		ResultSet result = db.QueryResult(query);
		
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
			
			db.FreeResult(result);
		}
	}
}
