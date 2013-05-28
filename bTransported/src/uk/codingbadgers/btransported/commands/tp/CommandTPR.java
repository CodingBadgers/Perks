package uk.codingbadgers.btransported.commands.tp;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.bFundamentals.player.FundamentalPlayer;
import uk.codingbadgers.btransported.bTransported;
import uk.codingbadgers.btransported.commands.tp.PlayerTPRData;

public class CommandTPR extends ModuleCommand {

	private bTransported m_module = null;

	public CommandTPR(bTransported module) {
		super("tpr", "tpr <playername>");
		m_module = module;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		
		if (!(sender instanceof Player)) {
			return true;
		}
		
		final Player player = (Player)sender;
		
		if (!Module.hasPermission(player, "perks.btransported.tpr")) {
			String formattedMessage = m_module.getLanguageValue("COMMAND-TP-NO-PERMISSION");
			formattedMessage = formattedMessage.replace("%permission%", "perks.btransported.tpr");
			Module.sendMessage("bTransported", player, formattedMessage);
			return true;
		}
		
		if (args.length == 1) {
			final String playerName = args[0];
			
			FundamentalPlayer requestPlayer = bFundamentals.Players.getPlayer(playerName);
			if (requestPlayer == null) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TPR-PLAYER-NOT-FOUND").replace("%playername%", playerName));
				return true;
			}
			
			PlayerTPRData TPRData = (PlayerTPRData)requestPlayer.getPlayerData(PlayerTPRData.class);
			if (TPRData == null) {
				TPRData = new PlayerTPRData();
			}
			
			TPRData.addTPRequest(player, requestPlayer.getPlayer());
	
			requestPlayer.addPlayerData(TPRData);
			
			// Tell sender the request was sent
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TPR-REQUEST-SENT").replace("%playername%", requestPlayer.getPlayer().getName()));
			
			// Tell the player that the sender sent a request
			Module.sendMessage("bTransported", requestPlayer.getPlayer(), m_module.getLanguageValue("COMMAND-TPR-RECEIVED-REQUEST").replace("%playername%", player.getName()));
			
			return true;
		}
		
		// invalid usage
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TPR-USAGE"));
		
		return true;
	}
	
}