/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.codingbadgers.btransported.commands.callbacks;

import java.util.Map.Entry;
import org.bukkit.Location;
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
public class SpawnGuiCallback implements GuiCallback {
    
    private final Player m_player;
    private final Entry<String, Location> m_spawn;
    private final bTransported m_module;
    
    /**
     *
     * @param module
     * @param player
     * @param spawn
     */
    public SpawnGuiCallback(bTransported module, Player player, Entry<String, Location> spawn) {
        m_player = player;
        m_spawn = spawn;
        m_module = module;
    }

    @Override
    public void onClick(GuiInventory inventory, InventoryClickEvent clickEvent) {
        
        // Close the inventory
        inventory.close(m_player);
        
		// Teleport the player
		if (m_module.teleportOfflinePlayer(m_player, m_spawn.getValue())) {
			Module.sendMessage("Spawn", m_player, m_module.getLanguageValue("COMMAND-SPAWN-TELEPORT-SUCCESS"));
		}
		else {
			Module.sendMessage("Spawn", m_player, m_module.getLanguageValue("COMMAND-SPAWN-TELEPORT-FAIL"));
		}
        
    }
    
}
