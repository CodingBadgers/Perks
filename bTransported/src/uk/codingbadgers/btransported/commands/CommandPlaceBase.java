/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.codingbadgers.btransported.commands;

import net.minecraft.server.v1_7_R3.Container;
import net.minecraft.server.v1_7_R3.ContainerAnvil;
import net.minecraft.server.v1_7_R3.ContainerAnvilInventory;
import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.PacketPlayOutOpenWindow;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_7_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.btransported.bTransported;

/**
 * Base class for home/spawn/warp commands
 * @author N3wton
 */
public abstract class CommandPlaceBase extends ModuleCommand implements Listener {

	protected final bTransported m_module;
	
	private final String m_anvilInventoryName;
	
	/**
	 * 
	 * @param module
	 * @param anvilInventoryName
	 * @param label
	 * @param usage 
	 */
	public CommandPlaceBase(bTransported module, String anvilInventoryName, String label, String usage) {
		super(label, usage);
		m_module = module;
		m_anvilInventoryName = anvilInventoryName;
	}
	
	/**
     * Handle click events within inventory's
     *
     * @param event The click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        
        Inventory invent = event.getInventory();
        if (invent.getType() != InventoryType.ANVIL) {
            return;
        }
        
        if (!invent.getName().equals(m_anvilInventoryName)) {
            return;
        }
        
        // we are in one of our anvil inventories
        event.setCancelled(true);
        
        // Are they clicking the result slot?
        final int ANVIL_RESULT_SLOT = 2;
        if (event.getRawSlot() != ANVIL_RESULT_SLOT) {
            return;
        }
        
        // Get the slot item
        ItemStack nameItem = invent.getItem(ANVIL_RESULT_SLOT);
        if (nameItem == null) {
            return;
        }

        // Setup the vars
        Player player = (Player) event.getWhoClicked();
        String name = nameItem.getItemMeta().hasDisplayName() ? nameItem.getItemMeta().getDisplayName() : player.getName() + "s Home";
        Location location = player.getLocation();
        
        // Add the new home
		onAnvilNameComplete(player, location, name);
        
        // Close the inventory        
        player.closeInventory();
        player.updateInventory();              
    }
    
    /**
     * Handle close events within inventory's
     *
     * @param event The close event
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        
        Inventory invent = event.getInventory();
        if (invent.getType() != InventoryType.ANVIL) {
            return;
        }
        
        if (!invent.getName().equals(m_anvilInventoryName)) {
            return;
        }
        
        // Remove all items from our home anvil
        invent.setContents(new ItemStack[] {});        
    }
	
	/**
	 * 
	 * @param player
	 * @param item
	 * @return 
	 */
	public boolean openAnvilInventory(Player player, ItemStack item) {

        // Get the entity player
        EntityPlayer ePlayer = (EntityPlayer)((CraftHumanEntity)player).getHandle();

        // Make an anvil and set the item in it
        ContainerAnvil anvilContainer = new ContainerAnvil(ePlayer.inventory, ePlayer.world, 0, 0, 0, ePlayer);
        if (item != null) {
            anvilContainer.setItem(0, CraftItemStack.asNMSCopy(item));
        }
        
        // Rename the inventory
        CraftInventoryAnvil craftInventoryAnvil = (CraftInventoryAnvil)anvilContainer.getBukkitView().getTopInventory();
        ContainerAnvilInventory containerAnvilInventory = (ContainerAnvilInventory)craftInventoryAnvil.getInventory();
        containerAnvilInventory.a(m_anvilInventoryName);
        
        // Fire an event just to be nice and make it cancelable
        Container container = CraftEventFactory.callInventoryOpenEvent(ePlayer, anvilContainer);
        if (container == null) {
            return false;
        }
        // Open the inventory to the player
        int containerCounter = ePlayer.nextContainerCounter();
        ePlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerCounter, 8, "Repairing", 9, true));
        ePlayer.activeContainer = container;
        ePlayer.activeContainer.windowId = containerCounter;
        ePlayer.activeContainer.addSlotListener(ePlayer);
        ePlayer.activeContainer.checkReachable = false;

        return true;
    }
	
	/**
     * Format a name into a non spaced camel case name
     * @param name The name to format
     * @return The formated version of the name
     */
    protected String formatName(String name) {
        
        name = name.toLowerCase();
        String[] nameParts = name.split(" ");
        String formattedName = "";
        for (String part : nameParts) {
            String camelPart = part.substring(0, 1).toUpperCase();
            if (part.length() > 1) {
                camelPart += part.substring(1);
            }
            formattedName += camelPart;
        }      
        
        return formattedName;
    }

	/**
	 * Called when naming an item has been completed on an anvil
	 * @param player
	 * @param location
	 * @param name 
	 */
	protected abstract void onAnvilNameComplete(Player player, Location location, String name);
}
