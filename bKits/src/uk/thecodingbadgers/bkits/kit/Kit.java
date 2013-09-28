package uk.thecodingbadgers.bkits.kit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Kit {

	private final String name;
	private List<ItemStack> items = new ArrayList<ItemStack>();
	private long timeout;
	
	private int cachecode;
	
	public Kit(String name, long timeout, List<ItemStack> items) {
		this.name = name;
		this.items = items;
		this.timeout = timeout;
	}
	
	public String getName() {
		return name;
	}
	
	public long getTimeout() {
		return timeout;
	}
	
	public List<ItemStack> getItems() {
		return items;
	}
	
	public void addItem(ItemStack stack) {
		items.add(stack);
		updateHashCode();
	}
	
	public void give(Player player) {
		for (ItemStack item : items) {
			player.getInventory().addItem(item);
		}
	}
	
	@Override
	public boolean equals(Object object) {
		return object instanceof Kit && 
				object.hashCode() == hashCode();
	}
	
	@Override
	public int hashCode() {
		return cachecode;
	}
	
	public void updateHashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(name);
		builder.append(timeout);
		builder.append(items.toArray(new ItemStack[0]));
		cachecode = builder.toHashCode();
	}
	
}
