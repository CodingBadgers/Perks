/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.thecodingbadgers.bkits.callbacks;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import uk.codingbadgers.bFundamentals.gui.GuiCallback;
import uk.codingbadgers.bFundamentals.gui.GuiInventory;

/**
 *
 * @author Sam
 */
public class KitPreviewGuiCallback implements GuiCallback {
	
	final private GuiInventory m_kitMenu;
	final private Player m_player;
	
	public KitPreviewGuiCallback(GuiInventory kitMenu, Player player) {
		m_kitMenu = kitMenu;
		m_player = player;
	}

	@Override
	public void onClick(GuiInventory inventory, InventoryClickEvent clickEvent) {
		
		inventory.close(m_player);
		m_kitMenu.open(m_player);
		
	}
	
}
