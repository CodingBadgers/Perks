package uk.codingbadgers.btransported.commands.tp;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.btransported.bTransported;

public class CommandSpawn extends ModuleCommand {
	
	private bTransported m_module = null;
	private HashMap<String, Location> m_spawn = new HashMap<String, Location>();

	public CommandSpawn(bTransported module) {
		super("spawn", "spawn | spawn <world name> | spawn <player_name> | spawn <world_name> <player_name>");
		m_module = module;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args)
	{
		if (!(sender instanceof Player)) {
			return true;
		}
		
		Player player = (Player)sender;
		
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
			
			teleportPlayer(player, player.getWorld().getName());
			
		// Handle /spawn <world name> and /spawn <player name>
		} else if (args.length == 1) {
			
			final String name = args[0];
			
			World world = Bukkit.getWorld(name);
			if (world != null) {
				// teleport the player who said the command to the spawn of the given world		
				
				if (!Module.hasPermission(player, "perks.btransported.spawn.world")) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-NO-PERMISSION").replace("%permission%", "perks.btransported.spawn.world"));
					return true;
				}
				
				teleportPlayer(player, world.getName());
				
				return true;
			}
			
			OfflinePlayer tpPlayer = Bukkit.getOfflinePlayer(name);
			if (tpPlayer != null) {
				// teleport the given player to the spawn of the command players world
				
				if (!Module.hasPermission(player, "perks.btransported.spawn.other")) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-NO-PERMISSION").replace("%permission%", "perks.btransported.spawn.other"));
					return true;
				}
				
				final String worldName = player.getWorld().getName();
				teleportPlayer(tpPlayer, worldName);
				
				if (!tpPlayer.isOnline()) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-COMPLETE").replace("%worldname%", worldName).replace("%playername%", name));				
				}
				
				return true;
			}
			
			// not a world or player
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-PLAYER-WORLD-NOT-FOUND").replace("%name%", name));
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-USAGE"));
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-ONE-PARAM-USAGE"));
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-TWO-PARAM-USAGE"));
			
			return true;
			
		// handle /spawn <worldname> <playername>
		} else if (args.length == 2) {
			
			if (!Module.hasPermission(player, "perks.btransported.spawn.other.world")) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-NO-PERMISSION").replace("%permission%", "perks.btransported.spawn.other.world"));
				return true;
			}
			
			final String playerName = args[0];
			final String worldName = args[1];
						
			OfflinePlayer tpPlayer = Bukkit.getOfflinePlayer(playerName);
			if (tpPlayer == null) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-PLAYER-NOT-FOUND").replace("%playername%", playerName));
				return true;
			}
			
			teleportPlayer(tpPlayer, worldName);
			
			if (!tpPlayer.isOnline()) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-SPAWN-COMPLETE").replace("%worldname%", worldName).replace("%playername%", playerName));				
			}
			
			return true;
		}
		
		return false;
	}
	
	private boolean teleportPlayer(OfflinePlayer player, String worldname) {
		
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
			player.getPlayer().teleport(location);				
			Module.sendMessage("bTransported", player.getPlayer(), m_module.getLanguageValue("COMMAND-SPAWN-COMPLETE").replace("%worldname%", worldname).replace("%playername%", player.getName()));
			return true;
		}
		
		// player is offline, edit there nbt data
		m_module.teleportOfflinePlayer(player, location);
			
		return true;		
	}
	
}
