package uk.thecodingbadgers.bkits.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.thecodingbadgers.bkits.bKits;
import uk.thecodingbadgers.bkits.kit.KitHandler;

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
		
		if (KitHandler.getInstance().handleKit(player, kit)) {
			bKits.sendMessage(bKits.getInstance().getName(), player, bKits.getInstance().getLanguageValue("given").replace("%kit%", kit));
		} else {		
			bKits.sendMessage(bKits.getInstance().getName(), player, bKits.getInstance().getLanguageValue("no-kit").replace("%kit%", kit));
		}
		return true;
	}

}
