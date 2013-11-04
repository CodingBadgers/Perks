package uk.codingbadgers.btransported.commands.tp;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.btransported.bTransported;

public class CommandTPHere extends ModuleCommand {

	private bTransported m_module = null;
	
	public CommandTPHere(bTransported module) {
		super("tphere", "tphere <player>");
		m_module = module;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args)
	{
		if (!(sender instanceof Player)) {
			return true;
		}
		
		final Player player = (Player)sender;
		
		if (!Module.hasPermission(player, "perks.btransported.tphere")) {
			String formattedMessage = m_module.getLanguageValue("COMMAND-TPHERE-NO-PERMISSION");
			formattedMessage = formattedMessage.replace("%permission%", "perks.btransported.tphere");
			Module.sendMessage("bTransported", player, formattedMessage);
			return true;
		}
		
		if (args.length == 0) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TPHERE-USAGE"));
			return true;
		}
		
		// Handle /tphere <playername>
		if (args.length == 1) {
			
			List<OfflinePlayer> playersWithName = m_module.matchPlayer(args[0], false);
			if (playersWithName.isEmpty()) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TP-PLAYER-NOT-FOUND"));
				return true;
			}
			
			String tpPlayerName = null;	
			
			if (playersWithName.size() == 1) {
				tpPlayerName = playersWithName.get(0).getName();
			}
			else {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TP-MULTIPLE-TARGETS"));
				String targetList = "";
				for (OfflinePlayer target : playersWithName) {
					targetList = targetList + target.getName() + ", ";
				}
				Module.sendMessage("bTransported", player, targetList.substring(0, targetList.length() - 2));
				return true;
			}
			
			OfflinePlayer tpPlayer = Bukkit.getServer().getOfflinePlayer(tpPlayerName);
			if (tpPlayer == null || !tpPlayer.hasPlayedBefore()) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TP-PLAYER-NOT-FOUND"));
				return true;
			}
			
			Location tpLocation = player.getLocation();
			if (tpPlayer.isOnline()) {
				
				if (!Module.hasPermission(player, "perks.btransported.tphere.player.online")) {
					String formattedMessage = m_module.getLanguageValue("COMMAND-TP-NO-PERMISSION");
					formattedMessage = formattedMessage.replace("%permission%", "perks.btransported.tphere.player.online");
					Module.sendMessage("bTransported", player, formattedMessage);
					return true;
				}
				
				tpPlayer.getPlayer().teleport(tpLocation);

			} else {
				
				if (!Module.hasPermission(player, "perks.btransported.tphere.player.offline")) {
					String formattedMessage = m_module.getLanguageValue("COMMAND-TP-NO-PERMISSION");
					formattedMessage = formattedMessage.replace("%permission%", "perks.btransported.tphere.player.offline");
					Module.sendMessage("bTransported", player, formattedMessage);
					return true;
				}
				
				m_module.teleportOfflinePlayer(tpPlayer, tpLocation);
			}
						
			String formattedMessage = m_module.getLanguageValue("COMMAND-TPHERE-PLAYER-SUCCESS");
			formattedMessage = formattedMessage.replace("%playername%", tpPlayerName);
			Module.sendMessage("bTransported", player, formattedMessage);
			
			return true;
		}
		
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TPHERE-USAGE"));
		return true;
	}

}
