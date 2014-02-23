/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.codingbadgers.btransported.commands.callbacks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import uk.codingbadgers.bFundamentals.gui.GuiCallback;
import uk.codingbadgers.bFundamentals.gui.GuiInventory;
import uk.codingbadgers.btransported.commands.CommandWarp;

/**
 *
 * @author Sam
 */
public class NewWarpGuiCallback implements GuiCallback {
   
    private final Player m_player;
    private final CommandWarp m_warpCommand;
    
    /**
     *
     * @param player
     * @param warpCommand
     */
    public NewWarpGuiCallback(Player player, CommandWarp warpCommand) {
        m_player = player;
        m_warpCommand = warpCommand;
    }

    @Override
    public void onClick(GuiInventory inventory, InventoryClickEvent clickEvent) {

        // Close the homes inventory
        inventory.close(m_player);
        
        // open an anvil gui for the name          
        ItemStack dummyEye = new ItemStack(Material.EYE_OF_ENDER);
        ItemMeta meta = dummyEye.getItemMeta();
        meta.setDisplayName("Enter Warp Name...");
        dummyEye.setItemMeta(meta);
        
		// Open an anvil inventory
        m_warpCommand.openAnvilInventory(m_player, dummyEye);
    }    
}
