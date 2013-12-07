package uk.thecodingbadgers.bkits.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.player.FundamentalPlayer;
import uk.thecodingbadgers.bkits.bKits;
import uk.thecodingbadgers.bkits.kit.KitHandler;
import uk.thecodingbadgers.bkits.kit.KitPlayerData;

public class KitCommand extends ModuleCommand {

	public KitCommand() {
		super("kit", "/kit <name>");
	}

	@Override
	public boolean onCommand(CommandSender sender, String labe, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(bKits.getInstance().getLanguageValue("not-player"));
			return true;
		}

		Player player = (Player) sender;

		if (args.length != 1) {
			KitHandler.getInstance().outputKits(player);
			return true;
		}

		String kit = args[0];

		switch (KitHandler.getInstance().handleKit(player, kit)) {
			case GIVEN:
				bKits.sendMessage(bKits.getInstance().getName(), player, bKits.getInstance().getLanguageValue("given").replace("%kit%", kit));
				break;
			case NOT_FOUND:
				bKits.sendMessage(bKits.getInstance().getName(), player, bKits.getInstance().getLanguageValue("not-given").replace("%kit%", kit));
				break;
			case TIMEOUT:
				FundamentalPlayer fplayer = bFundamentals.Players.getPlayer(player);
				KitPlayerData data = fplayer.getPlayerData(KitPlayerData.class);
				bKits.sendMessage(bKits.getInstance().getName(), player, bKits.getInstance().getLanguageValue("timeout").replace("%kit%", kit).replace("%time%", data.getFormmatedTime(kit)));
				break;
		}
		
		return true;
	}

	public static enum CommandResult {
		GIVEN, NOT_FOUND, TIMEOUT;
	}

}
