/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.codingbadgers.btransported.commands.home;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
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

        inventory.close(m_player);
        m_player.closeInventory();
        m_player.updateInventory();
        
        // open an anvil gui for the name
        /* TODO        
        ItemStack dummyBook = new ItemStack(Material.BOOK_AND_QUILL);
        ItemMeta meta = dummyBook.getItemMeta();
        meta.setDisplayName("Enter Home Name...");
        dummyBook.setItemMeta(meta);
        
        Inventory anvil = Bukkit.createInventory(m_player, InventoryType.ANVIL);
        anvil.addItem(dummyBook);
        m_player.openInventory(anvil);
        */
        
        m_homeCommand.addNewHome(m_player, m_location, "My Home");
        
    }
    
}
