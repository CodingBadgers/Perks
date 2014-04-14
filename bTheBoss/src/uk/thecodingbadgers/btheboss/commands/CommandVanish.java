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

public class CommandVanish extends ModuleCommand {

	public CommandVanish() {
		super("vanish", "/vanish");
		setDescription("Vanish into nothingness");
		setPermission("perks.btheboss.vanish");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		
		if (!(sender instanceof Player)) {
			outputMessage(sender, bTheBoss.getInstance().getLanguageValue("not-player"));
			return true;
		}
		
		Player player = (Player) sender;
		
		if (!Module.hasPermission(player, getPermission() + ".use")) {
			outputMessage(sender, bTheBoss.getInstance().getLanguageValue("no-permission").replace("%permission%", getPermission() + ".use"));
			return true;
		}
		
		if (player.hasMetadata("bTheBoss.vanished")) {
			// player is vanished
			player.removeMetadata("bTheBoss.vanished", bFundamentals.getInstance());	
			
			for (Player other : Bukkit.getOnlinePlayers()) {

				if (Module.hasPermission(other, getPermission() + ".see")) {
					outputMessage(other, bTheBoss.getInstance().getLanguageValue("vanish-shown-broadcast-override"));
					continue;
				}
				
				other.showPlayer(player);
				outputMessage(other, bTheBoss.getInstance().getLanguageValue("vanish-shown-broadcast"));
			}
			
			outputMessage(player, bTheBoss.getInstance().getLanguageValue("vanish-shown"));
		} else {
			// player isn't vanished
			player.setMetadata("bTheBoss.vanished", new FixedMetadataValue(bFundamentals.getInstance(), true));
			
			for (Player other : Bukkit.getOnlinePlayers()) {
				
				if (Module.hasPermission(other, getPermission() + ".see")) {
					outputMessage(other, bTheBoss.getInstance().getLanguageValue("vanish-vanished-broadcast-override"));
					continue;
				}
				
				other.hidePlayer(player);
				outputMessage(other, bTheBoss.getInstance().getLanguageValue("vanish-vanished-broadcast"));
			}
			
			outputMessage(player, bTheBoss.getInstance().getLanguageValue("vanish-vanished"));
		}
		
		return true;
	}
	
	public void outputMessage(CommandSender sender, String message) {	
		message = ChatColor.translateAlternateColorCodes('&', message);
		sender.sendMessage(ChatColor.DARK_PURPLE + "[" + bTheBoss.getInstance().getName() + "] " + ChatColor.RESET + message);
	}

}
