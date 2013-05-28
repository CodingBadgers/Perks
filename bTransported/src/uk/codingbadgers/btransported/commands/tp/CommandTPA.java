package uk.codingbadgers.btransported.commands.tp;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.bFundamentals.player.FundamentalPlayer;
import uk.codingbadgers.btransported.bTransported;

public class CommandTPA extends ModuleCommand {

	private bTransported m_module = null;

	public CommandTPA(bTransported module) {
		super("tpa", "tpa | tpa <playername>");
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
			
			Player lastTPPlayer = TPRData.getLastRequest();
			lastTPPlayer.teleport(player);
			TPRData.removeRequest(lastTPPlayer, player);
			
			// Tell player they have accepted the request
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TPA-ACCEPTED-REQUEST").replace("%playername%", lastTPPlayer.getName()));
			
			// Tell the sender that the request has been accepted
			Module.sendMessage("bTransported", lastTPPlayer, m_module.getLanguageValue("COMMAND-TPA-REQUEST-ACCEPTED").replace("%playername%", player.getName()));
			
			return true;
		}
		
		// invalid usage
		Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-TPA-USAGE"));
		
		return true;		
	}
}
