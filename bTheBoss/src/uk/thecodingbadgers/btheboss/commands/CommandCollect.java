package uk.thecodingbadgers.btheboss.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.btheboss.bTheBoss;

public class CommandCollect extends ModuleCommand {

	public CommandCollect() {
		super("collect", "/collect");
		setHelp("Collect your cleared inventory");
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
		
		MetadataValue value = target.getMetadata("bTheBoss.invenetory").get(0);
		ItemStack[] contents = (ItemStack[]) value.value();
		target.getInventory().clear();
		target.getInventory().setContents(contents);
		
		if (player != target) {
			outputMessage(target, bTheBoss.getInstance().getLanguageValue("clear-collect-other").replace("%target%", target.getName()));
			outputMessage(target, bTheBoss.getInstance().getLanguageValue("clear-collect-target").replace("%sender%", player.getName()));
		} else {
			outputMessage(target, bTheBoss.getInstance().getLanguageValue("clear-collect-own"));
		}
		
		return true;
	}
	
	public void outputMessage(CommandSender sender, String message) {	
		message = ChatColor.translateAlternateColorCodes('&', message);
		sender.sendMessage(ChatColor.DARK_PURPLE + "[" + bTheBoss.getInstance().getName() + "] " + ChatColor.RESET + message);
	}

}
