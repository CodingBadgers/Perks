package uk.thecodingbadgers.binvested.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import uk.thecodingbadgers.binvested.bInvested;

public class ScubaListener implements Listener {

	private static final short SCUBA_DAMAGE = 5;
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent event) {
		
		if (!(event.getEntity() instanceof Player) || event.getCause() != DamageCause.DROWNING) {
			return;
		}
		
		Player player = (Player) event.getEntity();
		
		if (!bInvested.hasPermission(player, "perks.binvested.scuba")) {
			return;
		}
		
		ItemStack helmet = player.getEquipment().getHelmet();
		
		if (helmet.getType() == Material.GOLD_HELMET) {
			helmet.setDurability((short) (helmet.getDurability() - SCUBA_DAMAGE));
		} else if (!bInvested.hasPermission(player, "perks.binvested.scuba.plus")) {
			return;
		}
		
		event.setCancelled(true);
		event.setDamage(0d);
		player.setRemainingAir(20);
	}
}
