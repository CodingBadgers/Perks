package uk.thecodingbadgers.binvested.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.binvested.bInvested;

public class CommandEnder extends ModuleCommand {

	public CommandEnder() {
		super("ender", "/ender [target]");

		setPermission("perks.binvested.ender");
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
		
		Player target = player;
		
		if (args.length > 0) {
			
			if (!Module.hasPermission(player, getPermission() + ".other")) {
				outputMessage(sender, bInvested.getInstance().getLanguageValue("no-permission").replace("%permission%", getPermission() + ".other"));
				return true;
			}
			
			target = Bukkit.getPlayer(args[0]);
			
			if (target == null) {
				outputMessage(sender, bInvested.getInstance().getLanguageValue("no-player"));
				return true;
			}
		}
		
		
		player.openInventory(target.getEnderChest());		
		outputMessage(player, bInvested.getInstance().getLanguageValue("ender-open" + (!target.equals(player) ? "-other" : "-own")).replace("%target%", target.getName()));		
		return true;
	}
	
	public void outputMessage(CommandSender sender, String message) {	
		message = ChatColor.translateAlternateColorCodes('&', message);
		sender.sendMessage(ChatColor.DARK_PURPLE + "[" + bInvested.getInstance().getName() + "] " + ChatColor.RESET + message);
	}

}
