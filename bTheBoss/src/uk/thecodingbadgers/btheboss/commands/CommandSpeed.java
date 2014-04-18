package uk.thecodingbadgers.btheboss.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.btheboss.bTheBoss;

public class CommandSpeed extends ModuleCommand {

	public CommandSpeed() {
		super("speed", "/speed <fly_speed> [target]");
		setDescription("Set your flying speed");
		setPermission("perks.btheboss.speed.use");
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

		if (args.length < 1) {
			outputMessage(sender, getUsage());
			return true;
		}
		
		Player target = player;
		
		if (args.length >= 2) {
			String name = args[1];

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
		
		String speed = args[0];
		float parsedSpeed = -1;
		
		try {
			parsedSpeed = Float.parseFloat(speed);
		} catch (NumberFormatException ex) {
			outputMessage(sender, bTheBoss.getInstance().getLanguageValue("not-number"));
			return true;
		}
		
		try {
			target.setFlySpeed(parsedSpeed);
		} catch (Exception ex) {
			outputMessage(sender, ex.getMessage());
			return true;
		}
		
		if (player != target) {
			outputMessage(sender, bTheBoss.getInstance().getLanguageValue("speed-set-other").replace("%target%", target.getName()).replace("%speed%", speed));
			outputMessage(target, bTheBoss.getInstance().getLanguageValue("speed-set-target").replace("%sender%", player.getName()).replace("%speed%", speed));
		} else {
			outputMessage(target, bTheBoss.getInstance().getLanguageValue("speed-set-own").replace("%speed%", speed));
		}
		
		return true;
	}
	
	public void outputMessage(CommandSender sender, String message) {	
		message = ChatColor.translateAlternateColorCodes('&', message);
		sender.sendMessage(ChatColor.DARK_PURPLE + "[" + bTheBoss.getInstance().getName() + "] " + ChatColor.RESET + message);
	}

}
