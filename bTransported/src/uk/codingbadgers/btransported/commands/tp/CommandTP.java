package uk.codingbadgers.btransported.commands.tp;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.btransported.bTransported;

import org.jnbt.*;

public class CommandTP extends ModuleCommand {
	
	private bTransported m_module = null;

	public CommandTP(bTransported module) {
		super("tp", "tp <x> <y> <z> [world] | tp <otherplayer>");
		m_module = module;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args)
	{
		if (!(sender instanceof Player)) {
			return true;
		}
		
		final Player player = (Player)sender;
		
		if (!Module.hasPermission(player, "perks.btransported.tp")) {
			String formattedMessage = m_module.getLanguageValue("COMMAND-TP-NO-PERMISSION");
			formattedMessage = formattedMessage.replace("%permission%", "perks.btransported.tp");
			Module.sendMessage("bTransported", player, formattedMessage);
			return true;
		}
		
		if (args.length == 0) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TP-USAGE"));
			return true;
		}
		
		// Handle /tp <x> <y> <z> [world]
		if (args.length == 3 || args.length == 4) {
			
			if (!Module.hasPermission(player, "perks.btransported.tp.coord")) {
				String formattedMessage = m_module.getLanguageValue("COMMAND-TP-NO-PERMISSION");
				formattedMessage = formattedMessage.replace("%permission%", "perks.btransported.tp.coord");
				Module.sendMessage("bTransported", player, formattedMessage);
				return true;
			}
			
			Double x = null, y = null, z = null;
			
			// get the xyz coords
			try {
				x = Double.parseDouble(args[0]);
				y = Double.parseDouble(args[1]);
				z = Double.parseDouble(args[2]);
			} catch(Exception ex) {
				// invalid numerical value
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TP-COORD-INVALID"));
				return true;
			}
			
			// get the world from the specified name
			World world = player.getWorld();
			if (args.length == 4) {
				
				if (!Module.hasPermission(player, "perks.btransported.tp.coord.world")) {
					String formattedMessage = m_module.getLanguageValue("COMMAND-TP-NO-PERMISSION");
					formattedMessage = formattedMessage.replace("%permission%", "perks.btransported.tp.coord.world");
					Module.sendMessage("bTransported", player, formattedMessage);
					return true;
				}
				
				final String worldName = args[3];
				World specifiedWorld = Bukkit.getWorld(worldName);
				if (specifiedWorld == null) {
					// invalid world name
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TP-COORD-INVALID-WORLDNAME"));
					return true;
				}
				else {
					world = specifiedWorld;
				}
			}
			
			String formattedMessage = m_module.getLanguageValue("COMMAND-TP-SUCCESS");
			formattedMessage = formattedMessage.replace("%location%", StringUtils.join(args, " "));
			Module.sendMessage("bTransported", player, formattedMessage);
			player.teleport(new Location(world, x, y, z));
			
			return true;
		}
		
		// Handle /tp <playername>
		if (args.length == 1) {
			
			final String tpPlayerName = args[0];
			OfflinePlayer tpPlayer = Bukkit.getServer().getOfflinePlayer(tpPlayerName);
			if (tpPlayer == null || !tpPlayer.hasPlayedBefore()) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TP-PLAYER-NOT-FOUND"));
				return true;
			}
			
			Location tpLocation = null;
			if (tpPlayer.isOnline()) {
				
				if (!Module.hasPermission(player, "perks.btransported.tp.player.online")) {
					String formattedMessage = m_module.getLanguageValue("COMMAND-TP-NO-PERMISSION");
					formattedMessage = formattedMessage.replace("%permission%", "perks.btransported.tp.player.online");
					Module.sendMessage("bTransported", player, formattedMessage);
					return true;
				}
				
				tpLocation = tpPlayer.getPlayer().getLocation();
			} else {
				
				if (!Module.hasPermission(player, "perks.btransported.tp.player.offline")) {
					String formattedMessage = m_module.getLanguageValue("COMMAND-TP-NO-PERMISSION");
					formattedMessage = formattedMessage.replace("%permission%", "perks.btransported.tp.player.offline");
					Module.sendMessage("bTransported", player, formattedMessage);
					return true;
				}
				
				// teleport to the locaiton of an offline player
				
				final String tpPlayerDatPath = Bukkit.getServer().getWorlds().get(0).getWorldFolder() + "/players/" + tpPlayer.getName() + ".dat";
				File tpPlayerDat = new File(tpPlayerDatPath);
				if (!tpPlayerDat.exists()) {
					// could not find offline player dat
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TP-PLAYER-NOT-FOUND"));
					m_module.log(Level.INFO, "Could not find offline player.dat at " + tpPlayerDatPath);
					return true;
				}
				
				InputStream inputStream = null;
				NBTInputStream nbtInputStream = null;
				
				try {
					inputStream = new FileInputStream(tpPlayerDat);
					nbtInputStream = new NBTInputStream(inputStream);
					
					Tag nbtTag = nbtInputStream.readTag();
					if (!(nbtTag instanceof CompoundTag)) {
						// not a compound tag, this is wrong
						Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TP-ERROR-UNKOWN"));
						m_module.log(Level.INFO, "Root NBT tag was not a compond tag for player '" + tpPlayerDatPath + "'.");
						return true;
					}
					
					CompoundTag rootTag = (CompoundTag)nbtTag;
					List<Tag> position = ((ListTag)rootTag.getValue().get("Pos")).getValue();
					if (position.size() != 3) {
						// there should be 3 elements in the Pos tag
						Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TP-ERROR-UNKOWN"));
						m_module.log(Level.INFO, "The Pos tag of the player '" + tpPlayerDatPath + "' does not have 3 coordinates.");
						return true;
					}
					
					Double x = (Double)position.get(0).getValue();
					Double y = (Double)position.get(1).getValue();
					Double z = (Double)position.get(2).getValue();
					
					Long worldLeast = ((LongTag)rootTag.getValue().get("WorldUUIDLeast")).getValue();
					Long worldMost = ((LongTag)rootTag.getValue().get("WorldUUIDMost")).getValue();
					World world = Bukkit.getWorld(new UUID(worldMost, worldLeast));
					
					tpLocation = new Location(world, x, y, z);					
					
				} catch (Exception ex) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TP-ERROR-UNKOWN"));
					ex.printStackTrace();
					return true;
				} finally {	
					try {
						nbtInputStream.close();
						inputStream.close();
					} catch (Exception ex) {
						Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TP-ERROR-UNKOWN"));
						ex.printStackTrace();
						return true;
					}
				}
			}
			
			if (tpLocation != null) {
				player.teleport(tpLocation);
				String formattedMessage = m_module.getLanguageValue("COMMAND-TP-PLAYER-SUCCESS");
				formattedMessage = formattedMessage.replace("%playername%", tpPlayerName);
				Module.sendMessage("bTransported", player, formattedMessage);
			} else {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TP-ERROR-UNKOWN"));
			}
			
			return true;
		}
		
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TP-USAGE"));
		return true;
	}
	

}
