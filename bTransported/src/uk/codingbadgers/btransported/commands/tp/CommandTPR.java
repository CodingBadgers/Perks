package uk.codingbadgers.btransported.commands.tp;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.player.FundamentalPlayer;
import uk.codingbadgers.btransported.bTransported;

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
		
		if (args.length == 1) {
			final Player player = (Player)sender;
			final String playerName = args[0];
			
			FundamentalPlayer requestPlayer = bFundamentals.Players.getPlayer(playerName);
			if (requestPlayer == null) {
				
				return true;
			}
			
			PlayerTPRData TPRData = (PlayerTPRData)requestPlayer.getPlayerData(PlayerTPRData.class);
			if (TPRData == null) {
				TPRData = new PlayerTPRData();
			}
			
			TPRData.addTPRequest(player, requestPlayer.getPlayer());
	
			requestPlayer.addPlayerData(TPRData);
			
			return true;
		}
		
		// invalid usage
		
		return true;
	}
	
}
