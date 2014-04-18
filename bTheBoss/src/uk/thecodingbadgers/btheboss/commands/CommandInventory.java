package uk.thecodingbadgers.btheboss.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.btheboss.bTheBoss;
import uk.thecodingbadgers.btheboss.inventory.InventoryAliases;

public class CommandInventory extends ModuleCommand {

	public CommandInventory() {
		super("inventory", "/inventory <item>[:datavalue] [amount] [target]");
		
		addAliase("item");
		addAliase("i");
		
		setDescription("Give yourself or another person an item");
		setPermission("perks.btheboss.inventory");
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
		
		if (args.length >= 3) {
			String name = args[2];

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
		
		Material material = Material.AIR;
		
		String matName = args[0];
		short dv = 0;
		
		if (matName.indexOf(':') != -1) {
			dv = Short.parseShort(matName.substring(matName.indexOf(':') + 1));
			matName = matName.substring(0, matName.indexOf(':'));
		}
		
		if (isInteger(matName)) {
			int id = Integer.parseInt(matName);
			material = Material.getMaterial(id);
		} else {
			material = InventoryAliases.getFromAlias(matName);
			
			if (material == null) {
				material = Material.matchMaterial(matName);			
			}
		}
		
		if (material == null) {
			outputMessage(sender, bTheBoss.getInstance().getLanguageValue("inv-no-item"));
			return true;
		}

		int ammount = 1;
		
		if (args.length >= 2) {
			try {
				ammount = Integer.parseInt(args[1]);
			} catch (NumberFormatException ex) {
				outputMessage(sender, bTheBoss.getInstance().getLanguageValue("not-number"));
				return true;		
			}
		}
		
		ItemStack stack = new ItemStack(material, ammount, dv);
		target.getInventory().addItem(stack);
		
		if (player != target) {
			outputMessage(sender, bTheBoss.getInstance().getLanguageValue("inv-give-other").replace("%target%", target.getName()).replace("%item%", material.name().toLowerCase()).replace("%ammount%", "" + ammount));
			outputMessage(target, bTheBoss.getInstance().getLanguageValue("inv-give-target").replace("%sender%", player.getName()).replace("%item%", material.name().toLowerCase()).replace("%ammount%", "" + ammount));
		} else {
			outputMessage(target, bTheBoss.getInstance().getLanguageValue("inv-give-own").replace("%item%", material.name().toLowerCase()).replace("%ammount%", "" + ammount));
		}
		
		return true;
	}
	
	public void outputMessage(CommandSender sender, String message) {	
		message = ChatColor.translateAlternateColorCodes('&', message);
		sender.sendMessage(ChatColor.DARK_PURPLE + "[" + bTheBoss.getInstance().getName() + "] " + ChatColor.RESET + message);
	}
	
	public boolean isInteger(String input) {  
       try {  
          Integer.parseInt(input);  
          return true;  
       } catch(Exception ex) {  
    	   return false;
       }  
    }

}
