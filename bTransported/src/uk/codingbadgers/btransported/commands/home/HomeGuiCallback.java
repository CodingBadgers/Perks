/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.codingbadgers.btransported.commands.home;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import uk.codingbadgers.bFundamentals.gui.GuiCallback;
import uk.codingbadgers.bFundamentals.gui.GuiInventory;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.btransported.bTransported;

/**
 *
 * @author N3wton
 */
public class HomeGuiCallback implements GuiCallback {
    
    private final Player m_player;
    private final PlayerHome m_home;
    private final bTransported m_module;
    
    /**
     *
     * @param module
     * @param player
     * @param home
     */
    public HomeGuiCallback(bTransported module, Player player, PlayerHome home) {
        m_player = player;
        m_home = home;
        m_module = module;
    }

    @Override
    public void onClick(GuiInventory inventory, InventoryClickEvent clickEvent) {
        
        // Close the inventory first
        inventory.close(m_player);
        
        // Teleport the player
        if (m_module.teleportOfflinePlayer(m_player, m_home.location)) {
            Module.sendMessage("Home", m_player, m_module.getLanguageValue("COMMAND-HOME-TELEPORT-SUCCESS"));
        }
        else {
            Module.sendMessage("Home", m_player, m_module.getLanguageValue("COMMAND-HOME-TELEPORT-FAIL"));
        }
        
    }
    
}
