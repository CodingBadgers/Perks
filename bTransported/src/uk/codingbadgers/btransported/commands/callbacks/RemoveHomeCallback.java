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
import uk.codingbadgers.btransported.commands.CommandHome;
import uk.codingbadgers.btransported.commands.home.PlayerHome;

/**
 *
 * @author Sam
 */
public class RemoveHomeCallback implements GuiCallback  {
	
	private final Player m_player;
    private final PlayerHome m_home;
    private final CommandHome m_command;

	public RemoveHomeCallback(CommandHome command, Player player, PlayerHome home) {
		m_player = player;
        m_home = home;
        m_command = command;
	}

	@Override
	public void onClick(GuiInventory inventory, InventoryClickEvent clickEvent) {
		m_command.handleHomeRemove(m_player, m_player.getName(), m_home.getName());
		m_command.handleHomeGUI(m_player);
	}
	
}
