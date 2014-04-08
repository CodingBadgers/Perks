package uk.thecodingbadgers.binvested.commands;

import net.minecraft.server.v1_7_R2.ContainerEnchantTable;
import net.minecraft.server.v1_7_R2.EntityPlayer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_7_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.binvested.bInvested;

public class CommandEnchant extends ModuleCommand {

	public CommandEnchant() {
		super("enchanting", "/enchanting");

		setPermission("perks.binvested.enchant");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			outputMessage(sender, bInvested.getInstance().getLanguageValue("not-player"));
			return true;
		}
		
		Player player = (Player) sender;
		
		if (!Module.hasPermission(player, getPermission() + ".own")) {
			outputMessage(sender, bInvested.getInstance().getLanguageValue("no-permission").replace("%permission%", getPermission() + ".own"));
			return true;
		}
		
		openEnchantingTable(player);
		outputMessage(player, bInvested.getInstance().getLanguageValue("enchant-open"));
		return true;
	}
	
	private void outputMessage(CommandSender sender, String message) {	
		message = ChatColor.translateAlternateColorCodes('&', message);
		sender.sendMessage(ChatColor.DARK_PURPLE + "[" + bInvested.getInstance().getName() + "] " + ChatColor.RESET + message);
	}
	
	private void openEnchantingTable(Player player) {
        EntityPlayer ePlayer = (EntityPlayer)((CraftHumanEntity)player).getHandle();
        
		InventoryView inv = player.openEnchanting(null, true);
		
		if (inv == null) {
			return;
		}
		
		ContainerEnchantTable container = (ContainerEnchantTable) ePlayer.activeContainer;
		if (player.getItemInHand() != null) {
			container.setItem(0, CraftItemStack.asNMSCopy(player.getItemInHand()));
			player.setItemInHand(new ItemStack(Material.AIR));
        }
        
	}
}
