package uk.thecodingbadgers.binvested.commands;

import net.minecraft.server.v1_7_R1.Container;
import net.minecraft.server.v1_7_R1.ContainerAnvil;
import net.minecraft.server.v1_7_R1.ContainerAnvilInventory;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.PacketPlayOutOpenWindow;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_7_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.binvested.bInvested;

public class CommandAnvil extends ModuleCommand {

	public CommandAnvil() {
		super("anvil", "/anvil");

		setPermission("perks.binvested.anvil");
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
		
		openAnvil(player);
		outputMessage(player, bInvested.getInstance().getLanguageValue("anvil-open"));
		return true;
	}
	
	private void outputMessage(CommandSender sender, String message) {	
		message = ChatColor.translateAlternateColorCodes('&', message);
		sender.sendMessage(ChatColor.DARK_PURPLE + "[" + bInvested.getInstance().getName() + "] " + ChatColor.RESET + message);
	}
	
	private void openAnvil(Player player) {
        // Get the entity player
        EntityPlayer ePlayer = (EntityPlayer)((CraftHumanEntity)player).getHandle();

        // Make an anvil and set the item in it
        ContainerAnvil anvilContainer = new ContainerAnvil(ePlayer.inventory, ePlayer.world, 0, 0, 0, ePlayer);
        if (player.getItemInHand() != null) {
            anvilContainer.setItem(0, CraftItemStack.asNMSCopy(player.getItemInHand()));
            player.setItemInHand(new ItemStack(Material.AIR));
        }
        
        // Rename the inventory
        CraftInventoryAnvil craftInventoryAnvil = (CraftInventoryAnvil)anvilContainer.getBukkitView().getTopInventory();
        ContainerAnvilInventory containerAnvilInventory = (ContainerAnvilInventory)craftInventoryAnvil.getInventory();
        containerAnvilInventory.a(ChatColor.BOLD + "Personal Anvil");
        
        // Fire an event just to be nice and make it cancelable
        Container container = CraftEventFactory.callInventoryOpenEvent(ePlayer, anvilContainer);
        if (container == null) {
            return;
        }
        // Open the inventory to the player
        int containerCounter = ePlayer.nextContainerCounter();
        ePlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerCounter, 8, "Repairing", 9, true));
        ePlayer.activeContainer = container;
        ePlayer.activeContainer.windowId = containerCounter;
        ePlayer.activeContainer.addSlotListener(ePlayer);
        ePlayer.activeContainer.checkReachable = false;
	}

}
