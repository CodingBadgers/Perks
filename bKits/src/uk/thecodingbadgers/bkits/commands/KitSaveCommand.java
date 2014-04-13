/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.thecodingbadgers.bkits.commands;

import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.bkits.bKits;

/**
 *
 * @author Sam
 */
public class KitSaveCommand extends ModuleCommand {

	final private String PERMISSION_KIT_SAVE = "perks.bkits.save";
	final private bKits m_module;
	
	public KitSaveCommand(bKits module) {
		super("kitsave", "/kitsave <name> <timeout-minutes>");
		m_module = module;
	}
	
	/**
	 * 
	 * @param sender
	 * @param label
	 * @param args
	 * @return 
	 */
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(m_module.getLanguageValue("not-player"));
			return true;
		}
		
		if (!Module.hasPermission(sender, PERMISSION_KIT_SAVE)) {
			Module.sendMessage("Kits", sender, m_module.getLanguageValue("COMMAND-BKITS-SAVE-NO-PERMISSION"));
			return true;
		}
		
		if (args.length != 2) {
			sender.sendMessage("Invalid Usage: " + this.getUsage());
			return true;
		}
		
		final Player player = (Player)sender;
		final PlayerInventory invent = player.getInventory();
		final String kitName = args[0];
		final Double kitTimeout = Double.parseDouble(args[1]);
		
		JSONObject newKit = new JSONObject();
		
		newKit.put("name", kitName);
		newKit.put("timeout-minutes", kitTimeout);
		
		JSONArray items = new JSONArray();
		for (int itemIndex = 9; itemIndex < invent.getSize(); ++itemIndex) {
			ItemStack item = invent.getItem(itemIndex);
			if (item == null || item.getType() == Material.AIR) {
				continue;
			}
			JSONObject itemObject = convertItemToObject(item, itemIndex - 9);
			items.add(itemObject);
		}
		newKit.put("items", items);
		
		m_module.addNewKit(kitName, newKit);
		m_module.reloadKits();
		Module.sendMessage("Kits", sender, "New kit created and saved.");

		return true;
	}

	/**
	 * 
	 * @param item
	 * @param slotIndex
	 * @return 
	 */
	private JSONObject convertItemToObject(ItemStack item, int slotIndex) {
		
		JSONObject itemObject = new JSONObject();
		ItemMeta meta = item.getItemMeta();
		
		// name
		if (meta.hasDisplayName()) {
			itemObject.put("name", meta.getDisplayName());
		}
		
		// material
		itemObject.put("material", item.getType().name());
		
		// amount
		itemObject.put("amount", item.getAmount());
		
		// data
		if (item.getData().getData() != 0) {
			itemObject.put("data", item.getData().getData());
		}
		
		// inventory slot
		itemObject.put("inventory-slot", slotIndex);
		
		// lore
		if (meta.hasLore()) {
			JSONArray lores = new JSONArray();
			for (String lore : meta.getLore()) {
				lores.add(lore);
			}
			itemObject.put("lore", lores);
		}
		
		// enchantments
		if (meta.hasEnchants()) {
			JSONArray enchants = new JSONArray();
			for (Entry<Enchantment, Integer> enchant : meta.getEnchants().entrySet()) {
				JSONObject objectEnchant = new JSONObject();
				objectEnchant.put("type", enchant.getKey().getName());
				objectEnchant.put("level", enchant.getValue());
				enchants.add(objectEnchant);
			}
			itemObject.put("enchantments", enchants);
		}
		
		return itemObject;
		
	}
}
