/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.codingbadgers.btransported.commands.callbacks;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import uk.codingbadgers.bFundamentals.gui.GuiCallback;
import uk.codingbadgers.bFundamentals.gui.GuiInventory;
import uk.codingbadgers.btransported.commands.CommandWarp;

/**
 *
 * @author Sam
 */
public class WarpGuiCallback implements GuiCallback {

    private final Player m_player;
    private final String m_warpname;
	private final CommandWarp m_command;
    
    /**
     *
     * @param player
     * @param warpname
	 * @param command
     */
    public WarpGuiCallback(Player player, String warpname, CommandWarp command) {
        m_player = player;
        m_warpname = warpname;
		m_command = command;
    }

	
	@Override
	public void onClick(GuiInventory inventory, InventoryClickEvent clickEvent) {
		
		inventory.close(m_player);
		m_command.warpPlayer(m_player, m_warpname);
		
	}
	
}
