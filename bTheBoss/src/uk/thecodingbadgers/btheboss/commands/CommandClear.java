package uk.thecodingbadgers.btheboss.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.btheboss.bTheBoss;

public class CommandClear extends ModuleCommand {

	public CommandClear() {
		super("clear", "/clear [target]");
		setHelp("Clear your inventory");
		setPermission("perks.btheboss.clear");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		
		if (!(sender instanceof Player)) {
			outputMessage(sender, bTheBoss.getInstance().getLanguageValue("not-player"));
			return true;
		}
		
		Player player = (Player) sender;
		
		if (!Module.hasPermission(player, getPermission() + ".own")) {
			outputMessage(sender, bTheBoss.getInstance().getLanguageValue("no-permission").replace("%permission%", getPermission() + ".own"));
			return true;
		}
		
		Player target = player;
		
		if (args.length >= 1) {
			String name = args[0];

			if (!Module.hasPermission(player, getPermission() + ".other")) {
				outputMessage(sender, bTheBoss.getInstance().getLanguageValue("no-permission").replace("%permission%", getPermission() + ".other"));
				return true;
			}
			
			target = Bukkit.getPlayer(name);
		}
		
		if (target == null || !target.isOnline()) {
			outputMessage(sender, bTheBoss.getInstance().getLanguageValue("no-player"));
			return true;
		}
		
		// back up their inventory just incase its hodgy being a twat
		target.setMetadata("bTheBoss.invenetory", new FixedMetadataValue(bFundamentals.getInstance(), player.getInventory().getContents()));
		target.getInventory().clear();
		
		if (player != target) {
			outputMessage(sender, bTheBoss.getInstance().getLanguageValue("clear-cleared-other").replace("%target%", target.getName()));
			outputMessage(target, bTheBoss.getInstance().getLanguageValue("clear-cleared-target").replace("%sender%", player.getName()));
		} else {
			outputMessage(target, bTheBoss.getInstance().getLanguageValue("clear-cleared-own"));
		}
		
		return true;
	}
	
	public void outputMessage(CommandSender sender, String message) {	
		message = ChatColor.translateAlternateColorCodes('&', message);
		sender.sendMessage(ChatColor.DARK_PURPLE + "[" + bTheBoss.getInstance().getName() + "] " + ChatColor.RESET + message);
	}

}
