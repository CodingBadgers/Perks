package uk.thecodingbadgers.binvested.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.binvested.bInvested;

public class CommandFly extends ModuleCommand {

	public CommandFly() {
		super("fly", "/fly");
		
		setPermission("perks.binvested.fly");
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
		
		if (args.length >= 2) {
			String name = args[1];

			if (!Module.hasPermission(player, getPermission() + ".other")) {
				outputMessage(sender, bInvested.getInstance().getLanguageValue("no-permission").replace("%permission%", getPermission() + ".other"));
				return true;
			}
			
			target = Bukkit.getPlayer(name);
		}
		
		if (target == null || !target.isOnline()) {
			outputMessage(sender, bInvested.getInstance().getLanguageValue("no-player"));
			return true;
		}
		
		boolean flying = true;
		
		if (target.getAllowFlight()) {
			flying = false;
		}
		
		try {
			target.setAllowFlight(flying);
		} catch (Exception ex) {
			outputMessage(sender, ex.getMessage());
			return true;
		}
		
		if (player != target) {
			outputMessage(sender, bInvested.getInstance().getLanguageValue("fly-" + (flying ? "flying" : "land") + "-other").replace("%target%", target.getName()));
			outputMessage(target, bInvested.getInstance().getLanguageValue("fly-" + (flying ? "flying" : "land") + "-target").replace("%sender%", player.getName()));
		} else {
			outputMessage(target, bInvested.getInstance().getLanguageValue("fly-" + (flying ? "flying" : "land") + "-own"));
		}
		
		return true;
	}
	
	public void outputMessage(CommandSender sender, String message) {	
		message = ChatColor.translateAlternateColorCodes('&', message);
		sender.sendMessage(ChatColor.DARK_PURPLE + "[" + bInvested.getInstance().getName() + "] " + ChatColor.RESET + message);
	}
}
