package uk.codingbadgers.btransported.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.btransported.bTransported;

/**
 * @author N3wton
 */
public class CommandHome extends ModuleCommand {
	
	/** The bFundamentals module */
	private bTransported m_module = null;
	
	/**  */
	private HashMap<String, HashMap<String, ArrayList<NamedLocation>>> m_homes = new HashMap<String, HashMap<String, ArrayList<NamedLocation>>>();

	/**
	 * Class constructor
	 * @param module	The bFundamentals module
	 */
	public CommandHome(bTransported module) {
		super("home", "home | home <home name> | home <player name> <home name> | home set <name> | home delete <home name> | home delete <player name> <home name>");
		m_module = module;
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
		if (!Module.hasPermission(player, "perks.btransported.home")) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-HOME-NO-PERMISSION").replace("%permission%", "perks.btransported.home"));
			return true;
		}
		
		// handle /home
		if (args.length == 0) {
			
			HashMap<String, ArrayList<NamedLocation>> playerHomes = m_homes.get(player.getName());
			if (playerHomes == null || playerHomes.isEmpty()) {
				// player has no homes
				return true;
			}
			
			final String worldName = player.getLocation().getWorld().getName();
			ArrayList<NamedLocation> worldHomes = playerHomes.get(worldName);
			if (worldHomes == null || worldHomes.isEmpty()) {
				// no homes in this world
				return true;
			}
			
			if (worldHomes.size() != 1) {
				// more than one home, list all home names in this world
				return true;
			}
			
			// teleport the player to there home in this world
			final NamedLocation location = worldHomes.get(0);
			teleportPlayer(player, location);
			return true;
		}
		// handle /home <home name>
		else if (args.length == 1) {
			
			HashMap<String, ArrayList<NamedLocation>> playerHomes = m_homes.get(player.getName());
			if (playerHomes == null || playerHomes.isEmpty()) {
				// player has no homes
				return true;
			}
			
			// Try find a home with the given name
			final String homeName = args[0];
			NamedLocation homeLocation = findHome(homeName, playerHomes);
			if (homeLocation == null) {
				// could not find a home with the name
				return true;
			}
			
			teleportPlayer(player, homeLocation);
			return true;			
		}
		// handle /home set <name> 
		// handle /home delete <home name>
		// handle /home <player name> <home name> 
		else if (args.length == 2) {
			
			// handle /home set <name> 
			if (args[0].equalsIgnoreCase("set")) {
				
				final String homeName = args[1];
				
				final Location location = player.getLocation();
				final String worldName = location.getWorld().getName();
	
				HashMap<String, ArrayList<NamedLocation>> playerHomes = m_homes.get(player.getName());
				
				if (playerHomes != null) {
					if (findHome(homeName, playerHomes) != null) {
						// home name already in use
						return true;
					}
					
					final NamedLocation newHome = new NamedLocation(homeName, location);
					
					ArrayList<NamedLocation> worldHomes = playerHomes.get(worldName);
					if (worldHomes == null) {
						// player has no homes in this world
						worldHomes = new ArrayList<NamedLocation>();
						worldHomes.add(newHome);
						playerHomes.put(worldName, worldHomes);
					}
					else {
						// add to list of homes for this world
						worldHomes.add(newHome);
					}
				} else {
					// player has no homes at all
					final NamedLocation newHome = new NamedLocation(homeName, location);
					
					playerHomes = new HashMap<String, ArrayList<NamedLocation>>();
					ArrayList<NamedLocation> worldHomes = new ArrayList<NamedLocation>();
					worldHomes.add(newHome);
					playerHomes.put(worldName, worldHomes);
					m_homes.put(player.getName(), playerHomes);
				}
								
				return true;
				
			} 
			// handle /home delete <home name>
			else if (args[0].equalsIgnoreCase("delete")) {
				
				final String homeName = args[1];
				HashMap<String, ArrayList<NamedLocation>> playerHomes = m_homes.get(player.getName());
				if (playerHomes == null) {
					// player has no homes
					return true;
				}
				
				if (!removeHome(homeName, player.getName())) {
					// no home found with that name
					return true;
				}
				
				return true;
				
			} 
			// handle /home <player name> <home name> 
			else {
				
				final String playerName = args[0];
				final String homeName = args[1];
				
				HashMap<String, ArrayList<NamedLocation>> playerHomes = m_homes.get(playerName);
				if (playerHomes == null) {
					// player has no homes
					return true;
				}
				
				NamedLocation playerHome = findHome(homeName, playerHomes);
				if (playerHome == null) {
					// player has no homes with that name
					return true;
				}
				
				teleportPlayer(player, playerHome);
				return true;
			}
			
		}
		// handle /home delete <player name> <home name>
		else if (args.length == 3) {
			
			final String playerName = args[1];
			final String homeName = args[2];
			
			HashMap<String, ArrayList<NamedLocation>> playerHomes = m_homes.get(playerName);
			if (playerHomes == null) {
				// player has no homes
				return true;
			}
			
			if (!removeHome(homeName, playerName)) {
				// no home found with that name
				return true;
			}
			
			return true;
			
		}
		
		return false;
	}

	private boolean removeHome(String homeName, String playerName) {
		
		HashMap<String, ArrayList<NamedLocation>> playerHomes = m_homes.get(playerName);
		if (playerHomes == null) {
			// player has no homes
			return false;
		}
		
		for (Entry<String, ArrayList<NamedLocation>> worldHome : playerHomes.entrySet()) {
			ArrayList<NamedLocation> worldHomes = worldHome.getValue();
			for (NamedLocation location : worldHomes) {
				if (location.name.equalsIgnoreCase(homeName)) {
					worldHomes.remove(location);
					return true;
				}
			}
		}
		
		return false;		
	}

	private NamedLocation findHome(String name, HashMap<String, ArrayList<NamedLocation>> homes) {
		
		for (Entry<String, ArrayList<NamedLocation>> worldHome : homes.entrySet()) {
			for (NamedLocation location : worldHome.getValue()) {
				if (location.name.equalsIgnoreCase(name)) {
					return location;
				}
			}
		}
		
		return null;
	}

	private void teleportPlayer(Player player, NamedLocation location) {
		
		// teleported player to home
		player.teleport(location.location);
		
	}
}
