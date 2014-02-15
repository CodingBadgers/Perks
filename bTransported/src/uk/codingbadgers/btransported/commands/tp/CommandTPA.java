package uk.codingbadgers.btransported.commands.tp;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.bFundamentals.player.FundamentalPlayer;
import uk.codingbadgers.btransported.bTransported;
import uk.codingbadgers.btransported.commands.tp.PlayerTPRData.TPRequest;

public class CommandTPA extends ModuleCommand {

	private bTransported m_module = null;

	public CommandTPA(bTransported module) {
		super("btpa", "btpa | btpa <playername>");
		m_module = module;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		
		if (!(sender instanceof Player)) {
			return true;
		}
	
		final Player player = (Player)sender;
		
		if (!Module.hasPermission(player, "perks.btransported.tpa")) {
			String formattedMessage = m_module.getLanguageValue("COMMAND-TP-NO-PERMISSION");
			formattedMessage = formattedMessage.replace("%permission%", "perks.btransported.tpa");
			Module.sendMessage("bTransported", player, formattedMessage);
			return true;
		}		
		
		FundamentalPlayer fPlayer = bFundamentals.Players.getPlayer(player);
		if (fPlayer == null) {
			return true;
		}
		
		if (args.length == 0) {
			
			PlayerTPRData TPRData = (PlayerTPRData)fPlayer.getPlayerData(PlayerTPRData.class);
			if (TPRData == null) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TPA-NO-REQUESTS"));
				return true;
			}
			
			TPRequest lastTPPlayer = TPRData.getLastRequest();
			if (lastTPPlayer == null) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TPA-NO-REQUESTS"));
				return true;
			}
			lastTPPlayer.from.teleport(player);
			TPRData.removeRequest(lastTPPlayer.from, player);
			
			// Tell player they have accepted the request
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TPA-ACCEPTED-REQUEST").replace("%playername%", lastTPPlayer.from.getName()));
			
			// Tell the sender that the request has been accepted
			Module.sendMessage("bTransported", lastTPPlayer.from, m_module.getLanguageValue("COMMAND-TPA-REQUEST-ACCEPTED").replace("%playername%", player.getName()));
			
			return true;
		}
		else if (args.length == 1) {
			
		}
		
		// invalid usage
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TPA-USAGE"));
		
		return true;		
	}
	
	/**
	 * Handle tab completion
	 */
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		
		List<String> matches = new ArrayList<String>();
		if (args.length == 0)
			return matches;
		
		if (!(sender instanceof Player)) {
			return matches;
		}
		
		Player player = (Player)sender;
		FundamentalPlayer fPlayer = bFundamentals.Players.getPlayer(player);
		if (fPlayer == null) {
			return matches;
		}
		
		PlayerTPRData TPRData = (PlayerTPRData)fPlayer.getPlayerData(PlayerTPRData.class);
		if (TPRData == null) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TPA-NO-REQUESTS"));
			return matches;
		}
		
		String name = args[args.length - 1];
		List<OfflinePlayer> players = m_module.matchPlayer(name, false);
		for (OfflinePlayer other : players) {
			if (!other.isOnline())
				continue;
			
			if (TPRData.requestExists(player, other.getPlayer()) || TPRData.requestExists(other.getPlayer(), player)) {
				matches.add(player.getName());
			}
		}
		
		return matches;
	}
}
