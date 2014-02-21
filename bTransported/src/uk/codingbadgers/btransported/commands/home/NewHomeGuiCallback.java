/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.codingbadgers.btransported.commands.home;

import net.minecraft.server.v1_7_R1.Container;
import net.minecraft.server.v1_7_R1.ContainerAnvil;
import net.minecraft.server.v1_7_R1.ContainerAnvilInventory;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.PacketPlayOutOpenWindow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_7_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import uk.codingbadgers.bFundamentals.gui.GuiCallback;
import uk.codingbadgers.bFundamentals.gui.GuiInventory;
import uk.codingbadgers.btransported.bTransported;
import uk.codingbadgers.btransported.commands.CommandHome;

/**
 *
 * @author N3wton
 */
public class NewHomeGuiCallback implements GuiCallback {
    
    private final bTransported m_module;
    private final Player m_player;
    private final Location m_location;
    private final CommandHome m_homeCommand;
    
    public NewHomeGuiCallback(bTransported module, Player player, CommandHome homeCommand) {
        m_module = module;
        m_player = player;
        m_location = player.getLocation();
        m_homeCommand = homeCommand;
    }

    @Override
    public void onClick(GuiInventory inventory, InventoryClickEvent clickEvent) {

        // Close the homes inventory
        inventory.close(m_player);
        
        // open an anvil gui for the name          
        ItemStack dummyBook = new ItemStack(Material.BOOK_AND_QUILL);
        ItemMeta meta = dummyBook.getItemMeta();
        meta.setDisplayName("Enter Home Name...");
        dummyBook.setItemMeta(meta);
        
        openAnvilInventory(m_player, dummyBook);
        
    }
    
    private boolean openAnvilInventory(Player player, ItemStack item) {

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
        containerAnvilInventory.a(CommandHome.ANVIL_INVENTORY_NAME);
        
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
    
}
