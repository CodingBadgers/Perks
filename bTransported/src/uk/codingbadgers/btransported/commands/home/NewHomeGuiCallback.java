/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.codingbadgers.btransported.commands.home;

import org.bukkit.Location;
import org.bukkit.Material;
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
    
    /**
     *
     * @param module
     * @param player
     * @param homeCommand
     */
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
        
		// Open an anvil inventory
        m_homeCommand.openAnvilInventory(m_player, dummyBook);
    }    
}
