package uk.thecodingbadgers.bkits.kit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Kit {

	private final String name;
	private List<ItemStack> items = new ArrayList<ItemStack>();
	
	public Kit(String name, int timeout, List<ItemStack> items) {
		this.name = name;
		this.items = items;
	}
	
	public String getName() {
		return name;
	}
	
	public List<ItemStack> getItems() {
		return items;
	}
	
	public void addItem(ItemStack stack) {
		items.add(stack);
	}
	
	public void give(Player player) {
		for (ItemStack item : items) {
			player.getInventory().addItem(item);
		}
	}
	
}
