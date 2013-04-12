package uk.thecodingbadgers.binvested.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.binvested.bInvested;

public class CommandCraft extends ModuleCommand {

	public CommandCraft() {
		super("craft", "/craft");

		setPermission("perks.binvested.craft");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {

		if (!(sender instanceof Player)) {
			outputMessage(sender, bInvested.getInstance().getLanguageValue("not-player"));
			return true;
		}
		
		Player player = (Player) sender;
		
		if (!Module.hasPermission(player, getPermission() + ".own")) {
			outputMessage(sender, bInvested.getInstance().getLanguageValue("no-permission").replace("%permission%", getPermission() + ".own"));
			return true;
		}
		
		player.openWorkbench(null, true);		
		outputMessage(player, bInvested.getInstance().getLanguageValue("craft-open"));		
		return true;
	}
	
	public void outputMessage(CommandSender sender, String message) {	
		message = ChatColor.translateAlternateColorCodes('&', message);
		sender.sendMessage(ChatColor.DARK_PURPLE + "[" + bInvested.getInstance().getName() + "] " + ChatColor.RESET + message);
	}
}
