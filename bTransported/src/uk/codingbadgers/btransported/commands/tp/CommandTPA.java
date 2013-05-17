package uk.codingbadgers.btransported.commands.tp;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
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
		FundamentalPlayer fPlayer = bFundamentals.Players.getPlayer(player);
		if (fPlayer == null) {
			return true;
		}
		
		if (args.length == 0) {
			
			PlayerTPRData TPRData = (PlayerTPRData)fPlayer.getPlayerData(PlayerTPRData.class);
			if (TPRData == null) {
				return true;
			}
			
			Player lastTPPlayer = TPRData.getLastRequest();
			lastTPPlayer.teleport(player);
			TPRData.removeRequest(lastTPPlayer, player);
			
			return true;
		}
		
		return true;		
	}
}
