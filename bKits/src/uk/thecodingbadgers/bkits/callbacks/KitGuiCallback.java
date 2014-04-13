/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.thecodingbadgers.bkits.callbacks;

import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.gui.GuiCallback;
import uk.codingbadgers.bFundamentals.gui.GuiInventory;
import uk.thecodingbadgers.bkits.Kit;
import uk.thecodingbadgers.bkits.bKits;

/**
 *
 * @author Sam
 */
public class KitGuiCallback implements GuiCallback {

	final private bKits m_module;
	final private Player m_player;
	final private Kit m_kit;
	final private boolean m_canClaim;
	
	/**
	 * 
	 * @param module
	 * @param player
	 * @param kit 
	 * @param canClaim
	 */
	public KitGuiCallback(bKits module, Player player, Kit kit, boolean canClaim) {
		m_module = module;
		m_player = player;
		m_kit = kit;
		m_canClaim = canClaim;
	}
	
	/**
	 * 
	 * @param inventory
	 * @param clickEvent 
	 */
	@Override
	public void onClick(GuiInventory inventory, InventoryClickEvent clickEvent) {
		
		if (clickEvent.isLeftClick() && m_canClaim) {
			
			m_module.giveKit(m_player, m_kit);
						
			inventory.close(m_player);
			m_player.closeInventory();
		}
		else if (clickEvent.isRightClick()) {
			
			// Preview Kit
			GuiInventory previewInvent = new GuiInventory(bFundamentals.getInstance());
			previewInvent.createInventory(m_kit.getName() + " Kit Preview", 3);
			
			Map<Integer, ItemStack> kitItems = m_kit.getItems();
			for (Entry<Integer, ItemStack> item : kitItems.entrySet()) {
				
				int slot = item.getKey();
				ItemStack previewItem = item.getValue().clone();
				ItemMeta meta = previewItem.getItemMeta();
				
				String name = previewItem.getType().name();
				if (meta.hasDisplayName()) {
					name = meta.getDisplayName();
				}
				
				String[] details = new String[] {};
				if (meta.hasLore()) {
					details = (String[]) meta.getLore().toArray();
				}
				
				previewInvent.addMenuItem(formatName(name), previewItem, details, slot, previewItem.getAmount(), new KitPreviewGuiCallback(inventory, m_player));
				
			}
			
			inventory.close(m_player);
			m_player.closeInventory();
			previewInvent.open(m_player);
			
		}
		
	}
	
	/**
	 * 
	 * @param raw
	 * @return 
	 */
	private String formatName(String raw) {
		
		String lower = raw.toLowerCase();
		String[] parts = lower.split("_");
		String formatted = "";
		for (String part : parts) {
			formatted += part.substring(0, 1).toUpperCase() + part.substring(1) + " ";
		}
		return formatted;
		
	}
	
}
