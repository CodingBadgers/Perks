/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.codingbadgers.btransported.commands.callbacks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.gui.GuiCallback;
import uk.codingbadgers.bFundamentals.gui.GuiInventory;
import uk.codingbadgers.bFundamentals.gui.callbacks.GuiReturnCallback;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.btransported.bTransported;
import uk.codingbadgers.btransported.commands.CommandHome;
import uk.codingbadgers.btransported.commands.home.PlayerHome;

/**
 *
 * @author N3wton
 */
public class HomeGuiCallback implements GuiCallback {
    
    private final Player m_player;
    private final PlayerHome m_home;
    private final bTransported m_module;
	private final CommandHome m_command;
    
    /**
     *
     * @param module
     * @param player
     * @param home
     */
    public HomeGuiCallback(bTransported module, Player player, PlayerHome home, CommandHome command) {
        m_player = player;
        m_home = home;
        m_module = module;
		m_command = command;
    }

    @Override
    public void onClick(GuiInventory inventory, InventoryClickEvent clickEvent) {
        
        // Close the inventory
        inventory.close(m_player);
        
		// Teleport the player
		if (clickEvent.isLeftClick()) {
			if (m_module.teleportOfflinePlayer(m_player, m_home.getLocation())) {
				Module.sendMessage("Home", m_player, m_module.getLanguageValue("COMMAND-HOME-TELEPORT-SUCCESS"));
			}
			else {
				Module.sendMessage("Home", m_player, m_module.getLanguageValue("COMMAND-HOME-TELEPORT-FAIL"));
			}	
		}
		// Show delete home menu
		else if (clickEvent.isRightClick()) {
			
			GuiInventory deleteHomeInventory = new GuiInventory(bFundamentals.getInstance());
			deleteHomeInventory.createInventory("Remove Home '" + m_home.getName() + "'", 3);
			
			// Add confirm dye
			ItemStack itemConfirm = new ItemStack(Material.INK_SACK, 1, (short)2);
			String[] confirmDetails = new String[1];
			confirmDetails[0] = ChatColor.GOLD + "WARNING: This can not be undone!";
			RemoveHomeCallback removecallback = new RemoveHomeCallback(m_command, m_player, m_home);
			deleteHomeInventory.addMenuItem(ChatColor.DARK_GREEN + "Remove " + m_home.getName(), itemConfirm, confirmDetails, 11, removecallback);
				
			// Add cancel dye
			ItemStack itemCancel = new ItemStack(Material.INK_SACK, 1, (short)1);
			deleteHomeInventory.addMenuItem(ChatColor.DARK_RED + "Cancel", itemCancel, new String[] {}, 15, new GuiReturnCallback(inventory));
			
			// Open the inventory
			deleteHomeInventory.open(m_player);
		}
        
    }
    
}
